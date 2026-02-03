package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.ScrollResponse;
import com.studylog.project.timer.TimerService;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final PlanRepositoryImpl planRepositoryImpl; //동적 쿼리용
    private final TimerService timerService;

    public PlanResponse getPlan(Long planId, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(planId, user);
        return PlanResponse.toDto(plan);
    }

    public ScrollPlanResponse searchTablePlans(UserEntity user, LocalDate startDate, LocalDate endDate,
                                               List<Long> categoryList, String keyword, Boolean status, List<String> sort,
                                               int page) {
        PlanSummary planSummary = planRepositoryImpl.getPlanSummaryByFilter(user, startDate, endDate, categoryList,
                keyword, status, sort, page);

        return getScrollPlanResponse(planSummary, page, user, null); //range: 일, 주, 월
    }

    // 메인에서 보여지는 계획은 today만 가능
    public ScrollPlanResponse MainDailyPlans(UserEntity user, LocalDate today, int page){
        PlanSummary planSummary = planRepositoryImpl.findTodayPlans(user, today, page);

        return getScrollPlanResponse(planSummary, page, user, "일");
    }

    public List<PlanResponse> getCalendarPlans(LocalDate startDate, LocalDate endDate, String range, UserEntity user){
        LocalDate sun, sat;
        boolean isSame = false;

        switch (range){
            case "weekly" -> {
                sun= startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                sat= sun.plusDays(6); //그 주의 토요일
                log.info("주 시작일: {}, 주 마지막 날: {}", sun, sat);
                if(startDate.equals(sun) && endDate.equals(sat)) isSame= true;
            }
            case "monthly" -> {
                log.info("monthly 조회");
                if(startDate.equals(startDate.with(TemporalAdjusters.firstDayOfMonth())) &&
                endDate.equals(endDate.with(TemporalAdjusters.lastDayOfMonth()))) {
                    log.info("isSame true");
                    isSame = true;
                }
            }
            default -> CommonThrow.invalidRequest("잘못된 범위 값: " + range);
        }

        if(!isSame) {
            CommonThrow.invalidRequest("잘못된 범위 값: " + range);
        }

        log.info("startDate {}, endDate {}", startDate, endDate);
        return planRepositoryImpl.getCalendarPlans(startDate, endDate, user);
    }

    private ScrollPlanResponse getScrollPlanResponse (PlanSummary planSummary, int page, UserEntity user, String range){
        List<PlanResponse> responses = planSummary.plans();
        long total= planSummary.totalCount(); // 오토 언박싱
        long achieved= planSummary.achivedCount(); // 오토 언박싱
        long totalStudyTime = planSummary.totalStudyTime();
        long pageSize = 10;

        //일, 주, 월 범위일 때만 메시지 함께 반환
        double rate = total == 0 ? 0.0 : (double) achieved / total * 100;
        String totalStudyTimeString= parseTotalStudyTimeFormat(totalStudyTime);

        boolean hasNext= page * pageSize < total;

        String message= returnMessage(user.getNickname(), rate, total, range);
        return ScrollPlanResponse.toDto(responses, achieved, total, rate, message, totalStudyTimeString, page, hasNext);
    }

    private String parseTotalStudyTimeFormat(Long totalSeconds){
        long hours= totalSeconds / 3600;
        long minutes= (totalSeconds % 3600) /60;
        long seconds= totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String returnMessage(String nickname, double rate, long total, String range){
        if(range == null) return null;

        //range는 day, week, month만 받음 (컨트롤러에서 분기 처리)
        String unit= range.equals("week")? "주":"달";
        if(total == 0) return "해당 일자에 등록된 계획이 없어요.";

        if (rate == 0.0){
            if(range.equals("day")) return "아직 달성한 계획이 없어요. 시작해 볼까요? 😎";
            return String.format("이번 %s에 달성한 계획이 없어요. 지금부터 해도 충분해요 🍀",
                    unit);
        } else if (rate < 50.0) {
            if(range.equals("day")) return String.format("시작이 제일 어려운 거 아시죠? %s 님은 그걸 해냈어요!",
                    nickname);
            return "천천히 쌓아가는 중이에요. 남은 기간 동안 더 쌓아봐요! 🏃";
        } else if (rate < 70) {
            if(range.equals("day")) return "계획의 반을 완료했어요! 잘하고 있어요 👏";
            return String.format("한 %s 목표의 절반 이상을 완료했어요! 조금만 더 힘내 볼까요? 🔥",
                    unit);
        } else if (rate < 100) {
            if(range.equals("day")) return "거의 다 했네요! 마무리만 잘하면 완벽해요 🔥";
            return String.format("한 %s간 열심히 달렸네요! 이제 마무리만 남았어요 👊",
                    unit);

        } else{
            if(range.equals("day")) return "오늘 계획을 모두 완료했어요! 최고예요!";
            return String.format("🎉 이번 %s 목표 달성! %s 님의 꾸준한 노력의 결과예요. 멋져요!",
                    unit, nickname);
        }
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        planRepository.updateCategory(deleteCategory, defaultCategory);
    }

    public PlanResponse addPlan(PlanRequest request, UserEntity user) {
        CategoryEntity category= getCategory(request.categoryId(), user);
        PlanEntity plan= request.toEntity(user, category);
        // flush 전이지만 저장된 엔티티 반환
        // IDENTIFY일 땐 id를 알 방법이 없으니까 저장 먼저 하고 id를 자바가 가짐
        PlanEntity savedPlan = planRepository.save(plan);
        return PlanResponse.toDto(savedPlan);

    }

    public PlanResponse updatePlan(Long id, PlanRequest request, UserEntity user) {
        //유저, 계획 검사
        PlanEntity plan= getPlanByUserAndId(id, user);
        CategoryEntity category= getCategory(request.categoryId(), user);
        //reqeust에 들어온 값 확인, 값이 있고 빈 문자열이 아닐 경우에만 처리 (시간은

        //타이머 있으면 처리, 없으면 패스
        timerService.getTimerByPlan(plan)
                        .ifPresent(timer -> {
                            timer.updateCategory(category);
                        });

        plan.updatePlan(request, category); //여기서 값 바뀐 거만 수정해줌
        return PlanResponse.toDto(plan);
    }

    //상태 변경 로직
    public void updateStatus(Long id, Boolean status, UserEntity user) {
        PlanEntity plan= getPlanByUserAndId(id, user);
        plan.updateStatus(status); //상태 변경
    }

    //삭제 로직
    public void deletePlan(Long id, UserEntity user) {
        PlanEntity plan= planRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        planRepository.delete(plan); //cascade로 타이머도 삭제됨
    }

    //타이머에 보내는 계획 리스트
    public ScrollResponse<PlansForTimerResponse> getPlansForTimer(LocalDate startDate, LocalDate endDate, String keyword,
                                                                  String sort, int page, UserEntity user){
        long pageSize = 20;

        if (!List.of("asc", "desc").contains(sort)) {
            CommonThrow.invalidRequest("잘못된 정렬 값: " + sort);
        }

        PlansForTimerSummary summary = planRepositoryImpl.getPlansForTimer(startDate, endDate, keyword, sort, page, user);
        List<PlansForTimerResponse> responses = summary.plans();
        Long totalItems = summary.totalItems();

        boolean hasNext= page * pageSize < totalItems;

        return new ScrollResponse<>(responses, totalItems, page, pageSize, hasNext);
    }

    //유저, planId 검사
    private PlanEntity getPlanByUserAndId(Long id, UserEntity user) {
        return planRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));
    }
    //유효성 검사 후 카테고리 가져옴
    private CategoryEntity getCategory(Long category, UserEntity user) {
        //유저에게 존재하지 않는 카테고리일 경우 반환 X
        //반환되는 카테고리도 영속 상태임 (@Transactional 써서 메서드 끝날 때까지 영속)
        return categoryRepository.findByUserAndId(user, category)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

}
