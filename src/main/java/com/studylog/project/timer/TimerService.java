package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.PageResponse;
import com.studylog.project.notification.NotificationEntity;
import com.studylog.project.notification.NotificationRepository;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.sse.SseEmitterService;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SseEmitterService sseEmitterService;
    private final TimerRepositoryImpl timerRepositoryImpl;
    private final NotificationRepository notificationRepository;
    //조건 조회
    public PageResponse<TimerResponse> searchTimers(UserEntity user, LocalDate startDate, LocalDate endDate,
                                                    List<Long> categoryList, String planKeyword, String keyword, String status, String sort,
                                                    int page) {

        long pageSize= 20;

       List<TimerResponse> timerResponses = timerRepositoryImpl.searchTimersByFilter
               (user, startDate, endDate, categoryList, planKeyword, keyword, status, sort, page);

        Long totalItems= timerRepositoryImpl.getTotalItems(user, startDate, endDate, categoryList, planKeyword,
                                                         keyword, status);

        long totalPages= (totalItems + pageSize - 1) / pageSize;
        return new PageResponse<>(timerResponses, totalItems, totalPages, page, pageSize);
    }

    //단일 조회
    public TimerResponse getTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        return TimerResponse.toDto(timer);
    }

    public TimerResponse createTimer(TimerRequest request, UserEntity user) {
        PlanEntity plan= null;
        CategoryEntity category;
        TimerEntity timer;

        if(request.planId() != null){
            plan= planRepository.findByUserAndId(user, request.planId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));
            checkPlan(plan, null, request);
            category= plan.getCategory();
        } else{ //request에 플랜 X
            category= categoryRepository.findByUserAndId(user, request.categoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        timer= request.toEntity(user, plan, category);
        timerRepository.saveAndFlush(timer); //이때 timer에도 id 매핑됨 (AI 된 값)

        if (plan != null) {
            return TimerResponse.toDto(timer, plan);
        }
        return TimerResponse.toDto(timer); //첫 생성 후 조회
    }

    /*
    원래 타이머의 계획이 없지 않는 이상, request 계획은 항상 들어옴 (수정 X일 경우 내 거로 채워서)
    우선, 내 플랜이 완료처리됨 -> 동일 계획으로 요청들어오면 수정 O
                         -> 다른 계획으로 요청 들어오면 수정 X
    플랜 완료 처리 안됐으면 수정할 계획이 완료 처리 안 됐으면 수정 O
    */
    public TimerResponse updateTimer(Long id, TimerRequest request, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id); //timer 존재 여부
        log.info("타이머: {}", timer.getId());
        PlanEntity plan= null;
        CategoryEntity category;

        if(request.planId() != null){
            plan= planRepository.findByUserAndId(user, request.planId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));
            checkPlan(plan, timer, request);
            category= plan.getCategory();
        } else { //request에 플랜 X
            if (timer.getPlan() != null && timer.getPlan().isComplete())
                throw new CustomException(ErrorCode.TIMER_PLAN_COMPLETED);
            category = categoryRepository.findByUserAndId(user, request.categoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        timer.updateName(request.name());
        timer.updatePlan(plan);
        timer.updateCategory(category);

        if (plan != null) {
            return TimerResponse.toDto(timer, plan);
        }
        return TimerResponse.toDto(timer);
    }

    public TimerResponse startTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        if(timer.getStatus().equals(TimerStatus.RUNNING))
            throw new CustomException(ErrorCode.TIMER_ALREADY_STATE);

        //갱신 안 되게 상태 조건 추가
        if(timerRepository.existsByUserAndStatus(user, TimerStatus.RUNNING))
            throw new CustomException(ErrorCode.TIMER_RUNNING);

        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new CustomException(ErrorCode.TIMER_ENDED);

        timer.start();
        if (timer.getPlan() != null) {
            return TimerResponse.toDto(timer, timer.getPlan());
        }
        return TimerResponse.toDto(timer);
    }

    //타이머 삭제 + 알림 경로 삭제
    public void deleteTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        //삭제한 타이머의 알림 받기 (영속 상태)
        List<NotificationEntity> notifications= notificationRepository.findAllByUserAndTimer(user, timer);
        for(NotificationEntity noti : notifications){ //해당 타이머를 가진 알림 없으면 패스
            noti.updateTimerId(); //null 처리
            if(!noti.getUrl().equals("/plans")) //타이머 url일 경우 삭제
                noti.updateUrl();
        }

        timerRepository.delete(timer);
    }

    //타이머 동기화
    //실행 중인 타이머의 실행 중인 랩 동기화
    public TimerResponse syncedTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(!timer.getStatus().equals(TimerStatus.RUNNING))
            throw new CustomException(ErrorCode.TIMER_NOT_RUNNING);
        timer.updateElapsed(getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
        timer.updateSyncedAt(); //동기화
        checkCompletion(timer, user, true);

        if (timer.getPlan() != null) {
            return TimerResponse.toDto(timer, timer.getPlan());
        }
        return TimerResponse.toDto(timer);
    }

    public TimerResponse pauseTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> timer.pause();
            case ENDED -> throw new CustomException(ErrorCode.TIMER_ENDED);
            default -> throw new CustomException(ErrorCode.TIMER_NOT_RUNNING);
        }

        timer.updateElapsed(getTotalElapsed(timer)); //누적 시간 갱신
        if(timer.getPlan() != null) {
            checkCompletion(timer, user, false);
            return TimerResponse.toDto(timer, timer.getPlan());
        }
        return TimerResponse.toDto(timer);
    }

    public TimerResponse endTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                timer.end(LocalDateTime.now());
                timer.updateElapsed(getTotalElapsed(timer)); //누적 시간 갱신
            }
            case ENDED -> throw new CustomException(ErrorCode.TIMER_ENDED);
            case READY -> throw new CustomException(ErrorCode.TIMER_NOT_RUNNING);
            case PAUSED -> timer.end(timer.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }
        if(timer.getPlan() != null) {
            checkCompletion(timer, user, false);
            return TimerResponse.toDto(timer, timer.getPlan());
        }
        return TimerResponse.toDto(timer);
    }

    //완료 체킹은 그대로
    public TimerResponse resetTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        if(timer.getPlan() != null && timer.getPlan().isComplete()) throw new CustomException(ErrorCode.TIMER_RESET_FOR_COMPLETED_PLAN);
        switch (timer.getStatus()) {
            case ENDED -> throw new CustomException(ErrorCode.TIMER_ENDED);
            case READY -> throw new CustomException(ErrorCode.TIMER_ALREADY_STATE);
            default -> timer.reset();
        }

        if (timer.getPlan() != null) {
            return TimerResponse.toDto(timer, timer.getPlan());
        }
        return TimerResponse.toDto(timer);
    }

    public TimerEntity getTimerByUserAndId(UserEntity user, Long id) {
        return timerRepository.getTimerWithPlanCategory(user, id).orElseThrow(() -> new CustomException(ErrorCode.TIMER_NOT_FOUND));
    }

    private void checkPlan(PlanEntity plan, TimerEntity timer, TimerRequest request) {
        if(timer == null || timer.getPlan() == null || !timer.getPlan().getId().equals(plan.getId())){
            if(timerRepository.existsByPlanId(plan.getId())) throw new CustomException(ErrorCode.TIMER_ALREADY_EXISTS);
            if(plan.isComplete()) throw new CustomException(ErrorCode.TIMER_PLAN_COMPLETED);
            if(!plan.getCategory().getId().equals(request.categoryId())){
                throw new CustomException(ErrorCode.TIMER_CATEGORY_PLAN_MISMATCH);
            }
        } else{ //타이머에 plan 있고 수정하려는 plan과 동일할 때
            if(!timer.getPlan().getCategory().getId().equals(request.categoryId()))
                throw new CustomException(ErrorCode.TIMER_CATEGORY_PLAN_MISMATCH);
        }
    }

    public long getTotalElapsed(TimerEntity entity) {
        LocalDateTime time= null;
        LocalDateTime startAt;

        if(entity.getSyncedAt() == null){ //동기화 전
            startAt = entity.getStartAt();
            switch (entity.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= entity.getPauseAt();
                case ENDED -> time= entity.getEndAt();
            }
        } else {
            startAt = entity.getSyncedAt();
            switch (entity.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= entity.getPauseAt();
                case ENDED -> time= entity.getEndAt();
            }
        }

        Duration duration= Duration.between(startAt, time);
        return duration.getSeconds() + entity.getElapsed();
    }

    private void checkCompletion(TimerEntity timer, UserEntity user, boolean isSyncCheck){
        LocalDate timerStartDate= timer.getSyncedAt() == null? timer.getStartAt().toLocalDate():timer.getSyncedAt().toLocalDate();
        LocalDate planStart= timer.getPlan().getStartDate();
        LocalDate planEnd= timer.getPlan().getEndDate();

        if(timer.getPlan().isComplete() || timer.getPlan().getMinutes() == 0) return; //이미 완료된 계획이거나 목표 시간이 0이라면
        //미완료 & 계획 일자 이후에 수행되었으면

        boolean inRange; //체킹 값 저장
        if(!isSyncCheck){ //정지, 종료에 의한 검사
            LocalDate timerEndDate= timer.getEndAt() == null? timer.getPauseAt().toLocalDate():timer.getEndAt().toLocalDate();
            inRange= (timerStartDate.isEqual(planStart) || timerStartDate.isAfter(planStart))
                    && (timerEndDate.isEqual(planEnd) || timerEndDate.isBefore(planEnd));
        } else { //동기화 후 검사
            inRange= (timerStartDate.isEqual(planStart) || timerStartDate.isAfter(planStart))
                    && (timerStartDate.isEqual(planEnd) || timerStartDate.isBefore(planEnd));
        }
        if(inRange && timer.getElapsed() >= timer.getPlan().getMinutes() * 60){
            timer.getPlan().updateStatus(true);
            sseEmitterService.alert(timer, user, isSyncCheck);
        }
    }

    public Optional<TimerEntity> getTimerByPlan(PlanEntity plan){
        return timerRepository.findByPlan(plan);
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        timerRepository.updateCategory(deleteCategory, defaultCategory);
    }

    // 연결이 끊겼을 때 호출될 이벤트 리스너 전용 메서드
    @Transactional
    public void pauseRunningTimerOnDisconnect(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<TimerEntity> runningTimers = timerRepository.findRunningTimerByUserId(user);

        for (TimerEntity timer : runningTimers) {
            //  상태 변경과 시간 갱신만 수행합니다.
            timer.pause();
            timer.updateElapsed(getTotalElapsed(timer));

            if (timer.getPlan() != null) {
                checkCompletion(timer, user, false);
            }
        }
    }

    @Scheduled(cron= "0 0/5 * * * *") //5분 간격 스케쥴링
    public void updateElapsedSecond() {
        //실행 중인 타이머만 조회
        List<TimerEntity> runningTimerList= timerRepository.findAllByStatus(TimerStatus.RUNNING);
        //timer 영속상태
        for (TimerEntity timer : runningTimerList) {
            timer.updateElapsed(getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
            timer.updateSyncedAt(); //자동 동기화
            if(timer.getPlan() != null){ //타이머에 계획이 있다면
               checkCompletion(timer, timer.getUser(), true);
            }
        }
    }

}

