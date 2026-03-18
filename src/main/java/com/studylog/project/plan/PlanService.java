package com.studylog.project.plan;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.CommonThrow;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.ScrollResponse;
import com.studylog.project.timer.TimerService;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import com.studylog.project.user.UserService;
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
    private final UserRepository userRepository;

    public PlanResponse getPlan(Long planId, Long userId) {
        PlanEntity plan= getPlanByUserAndId(planId, userId);

        if (plan.getTimer() != null) {
            return PlanResponse.toDto(plan, plan.getTimer());
        }

        return PlanResponse.toDto(plan);
    }

    public ScrollPlanResponse searchTablePlans(Long userId, LocalDate startDate, LocalDate endDate,
                                               List<Long> categoryList, String keyword, Boolean status, List<String> sort,
                                               int page) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        PlanSummary planSummary = planRepositoryImpl.getPlanSummaryByFilter(proxyUser, startDate, endDate, categoryList,
                keyword, status, sort, page);

        return getScrollPlanResponse(planSummary, page);
    }

    public List<PlanResponse> getCalendarPlans(LocalDate startDate, LocalDate endDate, String range, Long userId){
        UserEntity proxyUser = userRepository.getReferenceById(userId);

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
                if (startDate.isBefore(endDate)) {
                    isSame = true;
                }
            }

            default -> CommonThrow.invalidRequest("잘못된 범위 값: " + range);
        }

        if(!isSame) {
            CommonThrow.invalidRequest("잘못된 범위 값: " + range);
        }

        return planRepositoryImpl.getCalendarPlans(startDate, endDate, proxyUser);
    }

    private ScrollPlanResponse getScrollPlanResponse (PlanSummary planSummary, int page){
        List<PlanResponse> responses = planSummary.plans();
        long total= planSummary.totalCount(); // 오토 언박싱
        long achieved= planSummary.achivedCount(); // 오토 언박싱
        long totalStudyTime = planSummary.totalStudyTime();
        long pageSize = 10;

        //일, 주, 월 범위일 때만 메시지 함께 반환
        double rate = total == 0 ? 0.0 : (double) achieved / total * 100;
        String totalStudyTimeString= parseTotalStudyTimeFormat(totalStudyTime);

        boolean hasNext= page * pageSize < total;

        return ScrollPlanResponse.toDto(responses, achieved, total, rate, totalStudyTimeString, page, hasNext);
    }

    private String parseTotalStudyTimeFormat(Long totalSeconds){
        long hours= totalSeconds / 3600;
        long minutes= (totalSeconds % 3600) /60;
        long seconds= totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        planRepository.updateCategory(deleteCategory, defaultCategory);
    }

    public PlanResponse addPlan(PlanRequest request, Long userId) {
        // select 안 하고 JPA 속일 프록시 객체 생성
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        CategoryEntity category= getCategory(request.categoryId(), userId);

        PlanEntity plan= request.toEntity(proxyUser, category);
        // flush 전이지만 저장된 엔티티 반환
        // IDENTIFY일 땐 id를 알 방법이 없으니까 저장 먼저 하고 id를 자바가 가짐
        return PlanResponse.toDto(planRepository.save(plan));

    }

    public PlanResponse updatePlan(Long id, PlanRequest request, Long userId) {
        //유저, 계획 검사
        PlanEntity plan= getPlanByUserAndId(id, userId);
        CategoryEntity category= getCategory(request.categoryId(), userId);
        //reqeust에 들어온 값 확인, 값이 있고 빈 문자열이 아닐 경우에만 처리 (시간은

        //타이머 있으면 처리, 없으면 패스
        timerService.getTimerByPlan(plan)
                        .ifPresent(timer -> {
                            timer.updateCategory(category);
                        });

        plan.updatePlan(request, category); //여기서 값 바뀐 거만 수정해줌
        if (plan.getTimer() != null) {
            return PlanResponse.toDto(plan, plan.getTimer());
        }
        return PlanResponse.toDto(plan);
    }

    //상태 변경 로직
    public PlanResponse updateStatus(Long id, Boolean status, Long userId) {
        PlanEntity plan= getPlanByUserAndId(id, userId);
        plan.updateStatus(status); //상태 변경

        if (plan.getTimer() != null) {
            return PlanResponse.toDto(plan, plan.getTimer());
        }

        return PlanResponse.toDto(plan);
    }

    //삭제 로직
    public void deletePlan(Long id, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        PlanEntity plan= planRepository.findByUserAndId(proxyUser, id)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        planRepository.delete(plan); //cascade로 타이머도 삭제됨
    }

    //타이머에 보내는 계획 리스트
    public ScrollResponse<PlansForTimerResponse> getPlansForTimer(LocalDate startDate, LocalDate endDate, String keyword,
                                                                  String sort, int page, Long userId){
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        long pageSize = 20;

        if (!List.of("asc", "desc").contains(sort)) {
            CommonThrow.invalidRequest("잘못된 정렬 값: " + sort);
        }

        PlansForTimerSummary summary = planRepositoryImpl.getPlansForTimer(startDate, endDate, keyword, sort, page, proxyUser);
        List<PlansForTimerResponse> responses = summary.plans();
        Long totalItems = summary.totalItems();

        boolean hasNext= page * pageSize < totalItems;

        return new ScrollResponse<>(responses, page, totalItems, hasNext);
    }

    //유저, planId 검사
    public PlanEntity getPlanByUserAndId(Long id, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        // 프록시 객체 넘겨줌
        return planRepository.findByUserAndId(proxyUser, id)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));
    }
    //유효성 검사 후 카테고리 가져옴
    private CategoryEntity getCategory(Long category, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        //유저에게 존재하지 않는 카테고리일 경우 반환 X
        //반환되는 카테고리도 영속 상태임 (@Transactional 써서 메서드 끝날 때까지 영속)
        return categoryRepository.findByUserAndId(proxyUser, category)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

}
