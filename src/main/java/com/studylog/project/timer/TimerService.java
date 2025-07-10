package com.studylog.project.timer;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.sse.EventPayload;
import com.studylog.project.sse.SseEmitterService;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final SseEmitterService sseEmitterService;
    private record PlanAndCategory(PlanEntity plan, CategoryEntity category) {} //이 클래스에서만 쓸 거라 static 안 붙임

    //단일 조회
    public TimerResponse getTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        return TimerResponse.toDto(timer);
    }

    public TimerResponse createTimer(TimerRequest request, UserEntity user) {

        if(request.getPlan() != null)
            checkDuplicatePlan(null, request.getPlan());

        //유저 검증 & 계획-카테고리 검증
        PlanAndCategory pac= checkPlanAndCategory(request, user);
        PlanEntity plan= pac.plan();
        CategoryEntity category= pac.category();

        //계획이 있어도 카테고리랑 계획 일치하는지 검증해서 문제 없을 것
        //계획에 딸린 카테고리로 타이머 카테고리도 설정됨
        TimerEntity timer= request.toEntity(user, plan, category);

        timerRepository.saveAndFlush(timer); //이때 timer에도 id 매핑됨 (AI 된 값)
        return TimerResponse.toDto(timer); //첫 생성 후 조회
        }

    //타이머 업데이트 (실행 중엔 변경 X)
    /* 기존 계획id != 수정 계획id, 기존 계획 카테고리 != 수정 계획 카테고리 -> 계획 + 카테고리 검사
       기존 계획 != 수정 계획, 기존 계획 카테고리 == 수정 계획 카테고리 -> 계획만 변경
       -> 카테고리 서로 다른 거 확인하려면 check 메서드 타야 함
       계획, 카테고리 바뀐 거 없으면 타이머 이름만 변경
     */
    public TimerResponse updateTimer(Long id, TimerRequest request, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id); //timer 존재 여부

        if (timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머는 수정이 불가합니다");
        if (timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 타이머는 수정이 불가합니다");
        if(request.getPlan() != null)
            checkDuplicatePlan(timer, request.getPlan());

        PlanAndCategory pac= checkPlanAndCategory(request, user);
        PlanEntity plan= pac.plan(); //입력된 plan 없으면 null
        CategoryEntity category= pac.category();
        timer.updateTimerName(request.getName());
        timer.updatePlan(plan);
        timer.updateCategory(category);
        return TimerResponse.toDto(timer);
    }

    public TimerResponse startTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        if(timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("이미 실행 중인 타이머입니다.");

        //갱신 안 되게 상태 조건 추가
        if(timerRepository.existsByUserAndStatus(user, TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 있습니다. 정지/종료 후 다시 시도해 주세요.");

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case ENDED -> throw new BadRequestException("종료한 타이머는 재실행이 불가합니다.");
            case READY -> timer.startTimer(); //첫 실행
            case PAUSED -> timer.updateRestartTimer(); //재시작
        }

        return TimerResponse.toDto(timer);
    }

    //타이머 삭제
    public void deleteTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        timerRepository.delete(timer);
    }

    public TimerResponse pauseTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> timer.updatePauseTimer();
            case ENDED -> throw new BadRequestException("종료된 타이머는 정지가 불가합니다.");
            case READY -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
            case PAUSED -> throw new BadRequestException("이미 정지된 타이머입니다.");
        }

        //status= PAUSED로 바뀜
        log.info("pauseTimer: {}", timer.getStatus().toString());
        timer.updateElapsedSecond(getTotalElapsed(timer)); //누적 시간 갱신

        return TimerResponse.toDto(timer);
    }

    public TimerResponse endTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                timer.updateEndTimer(LocalDateTime.now());
                timer.updateElapsedSecond(getTotalElapsed(timer)); //누적 시간 갱신
            }
            case ENDED -> throw new BadRequestException("이미 종료된 타이머입니다.");
            case READY -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
            case PAUSED -> timer.updateEndTimer(timer.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }

        return TimerResponse.toDto(timer);
    }

    public TimerResponse resetTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        switch (timer.getStatus()) {
            case ENDED -> throw new BadRequestException("종료된 타이머는 초기화가 불가합니다.");
            case READY -> throw new BadRequestException("초기화 상태입니다.");
            default -> timer.resetTimer();
        }
        log.info("타이머 초기화 완료");
        return TimerResponse.toDto(timer);
    }

    //초 단위 경과 시간 넘김 (현재 누적 시간 + 이전 누적 시간)
    private Long getTotalElapsed(TimerEntity timer) {
        LocalDateTime time= null;
        //재시작 시간이 null이라면 첫 시작 시간 불러옴 (정지한 적 없다는 뜻)
        LocalDateTime startAt = timer.getRestartAt() == null ? timer.getStartAt() : timer.getRestartAt();

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> time = LocalDateTime.now();
            case PAUSED -> time= timer.getPauseAt();
            case ENDED -> time= timer.getEndAt();
        }

        Duration duration= Duration.between(startAt, time);
        return duration.getSeconds() + timer.getElapsedSecond();
    }

    private TimerEntity getTimerByUserAndId(UserEntity user, Long id) {
        return timerRepository.findByUserAndId(user, id).orElseThrow(() -> new NotFoundException("존재하지 않는 타이머입니다."));
    }

    //계획-카테고리 검증
    private PlanAndCategory checkPlanAndCategory(TimerRequest request, UserEntity user) {
        PlanEntity plan= null;
        CategoryEntity category= null;

        if (request.getPlan() != null){
            plan= planRepository.findByUserAndId(user, request.getPlan())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
            if(!plan.getCategory().getId().equals(request.getCategory())){ //달라도 입력 카테고리 무시하긴 하지만 일관성을 위해 넣음
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
            }
            category= plan.getCategory();
        } else { //plan == null, 카테고리만 검색
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                    .orElseThrow(()-> new NotFoundException("존재하지 않는 카테고리입니다."));
        }
        return new PlanAndCategory(plan, category); //입력된 plan 없으면 null 반환
    }

    //계획 중복 검사
    private void checkDuplicatePlan(TimerEntity timer, Long planId) {
        if (timerRepository.existsByPlanId(planId)) {
            if(timer == null) throw new BadRequestException("선택한 계획의 타이머가 이미 존재합니다.");
            else { //타이머가 있을 때 겹치면 (현재 타이머의 계획과 수정할 계획이 같으면 문제 X)
                if(timer.getPlan() == null || !timer.getPlan().getId().equals(planId)) { //타이머 엔티티의 계획이 null이면 다른 타이머와 겹친다는 것
                    throw new BadRequestException("선택한 계획의 타이머가 이미 존재합니다.");
                }
            }
        }
    }

    @Scheduled(cron= "0 0/5 * * * *") //5분 간격 스케쥴링
    public void updateElapsedSecond() {
        LocalDateTime now = LocalDateTime.now();
        List<TimerEntity> runningTimerList= timerRepository.findAllByStatus(TimerStatus.RUNNING);

        for (TimerEntity timer : runningTimerList) {
            if(timer.getStatus().equals(TimerStatus.RUNNING)) { //실행 중 타이머라면
                timer.updateSyncedAt(); //자동 동기화
                LocalDateTime startAt = timer.getRestartAt() == null ? timer.getStartAt() : timer.getRestartAt();
                long duration = Duration.between(startAt, now).getSeconds();
                if(timer.getPlan() != null){ //타이머에 계획이 있다면
                    if (duration >= timer.getPlan().getMinutes() * 60) {
                        EventPayload payload = new EventPayload();
                        payload.setType("plan-completed");
                        payload.setId(timer.getUser().getUser_id());
                        payload.setMessage("계획 목표 달성 시간을 채웠습니다! 타이머를 정지 및 종료하면 계획이 완료돼요");
                        sseEmitterService.broadcast(timer.getUser(), payload);
                    } //타이머에 계획 없으면 동기화만..
                }
            }
        }
    }
}

