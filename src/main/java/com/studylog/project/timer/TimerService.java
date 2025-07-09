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
    private static record PlanAndCategory(PlanEntity plan, CategoryEntity category) {}

    //단일 조회
    public TimerResponse getTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        return TimerResponse.toDto(timer);
    }

    public TimerResponse createTimer(TimerRequest request, UserEntity user) {

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

    //타이머 업데이트 (정지 or 종료 시에만 가능)
    public TimerResponse updateTimer(Long id, TimerRequest request, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id); //timer 존재 여부
        /* 기존 계획 != 수정 계획, 기존 계획 카테고리 != 수정 계획 카테고리 -> 계획 + 카테고리 검사
           기존 계획 != 수정 계획, 기존 계획 카테고리 == 수정 계획 카테고리 -> 계획만 변경
           계획, 카테고리 바뀐 거 없으면 타이머 이름만 변경
         */
        if (timer.getPlan().getId().equals(request.getPlan())) {

        }
        //
        PlanAndCategory pac= checkPlanAndCategory(request, user);


    }
    public TimerResponse startTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        //갱신 안 되게 상태 조건 추가
        if(timerRepository.existsByUserAndStatus(user, TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 있습니다. 정지/종료 후 다시 시도해 주세요.");
        if(timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("이미 실행 중인 타이머입니다.");
        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료한 타이머는 재실행이 불가합니다.");

        if(timer.getStatus().equals(TimerStatus.READY)) //첫 실행 시
            timer.startTimer();
        else if (timer.getStatus().equals(TimerStatus.PAUSED))  //재시작 시
            timer.updateRestartTimer();

        return TimerResponse.toDto(timer);
    }

    public TimerResponse pauseTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(timer.getStatus().equals(TimerStatus.READY))
            throw new BadRequestException("실행 중인 타이머가 아닙니다.");
        if(timer.getStatus().equals(TimerStatus.PAUSED))
            throw new BadRequestException("이미 정지된 타이머입니다.");
        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 타이머는 정지가 불가합니다.");

        timer.updatePauseTimer();
        //status= PAUSED
        timer.updateElapsedSecond(getTotalElapsed(timer)); //누적 시간 갱신

        return TimerResponse.toDto(timer);
    }

    public TimerResponse endTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(timer.getStatus().equals(TimerStatus.READY))
            throw new BadRequestException("실행 중인 타이머가 아닙니다.");
        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("이미 종료된 타이머입니다.");

        if(timer.getStatus().equals(TimerStatus.PAUSED)) //정지된 타이머라면, 정지 시간 받음
            timer.updateEndTimer(timer.getPauseAt());
        else if(timer.getStatus().equals(TimerStatus.RUNNING))
            timer.updateEndTimer(LocalDateTime.now());

        //status= ENDED
        timer.updateElapsedSecond(getTotalElapsed(timer)); //누적 시간 갱신
        return TimerResponse.toDto(timer);
    }

    //초 단위 경과 시간 넘김 (현재 누적 시간 + 이전 누적 시간)
    private Long getTotalElapsed(TimerEntity timer) {
        LocalDateTime time= null; //나중에 else 만들어서 Now로 처

        //재시작 시간이 null이라면 첫 시작 시간 불러옴 (정지한 적 없다는 뜻)
        LocalDateTime startAt = timer.getRestartAt() == null ? timer.getStartAt() : timer.getRestartAt();
        if(timer.getStatus().equals(TimerStatus.READY))
            return 0L; //쓸 일 없나..? 내일 확인
        if(timer.getStatus().equals(TimerStatus.RUNNING))
            time = LocalDateTime.now();
        if(timer.getStatus().equals(TimerStatus.PAUSED))
            time= timer.getPauseAt();
        if(timer.getStatus().equals(TimerStatus.ENDED))
            time= timer.getEndAt();

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
            log.info("계획 입력됨");
            plan= planRepository.findByUserAndId(user, request.getPlan())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
            log.info("계획 검증 통과 O");
            if(!plan.getCategory().getId().equals(request.getCategory())){ //달라도 입력 카테고리 무시하긴 하지만 일관성을 위해 넣음
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
            }
        } else { //plan == null, 카테고리만 검색
            log.info("입력된 계획 없음");
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                    .orElseThrow(()-> new NotFoundException("존재하지 않는 카테고리입니다."));
            log.info("카테고리 검증 통과 O");
        }
        return new PlanAndCategory(plan, category);
    }

    @Scheduled(fixedRate = 60 * 1000) //1분 간격 스케쥴링
    public void updateElapsedSecond() {
        LocalDateTime now = LocalDateTime.now();
        List<TimerEntity> runningTimerList= timerRepository.findAllByStatus(TimerStatus.RUNNING);

        for (TimerEntity timer : runningTimerList) {
            if(timer.getStatus().equals(TimerStatus.RUNNING) && timer.getSyncedAt() == null) {
                //실행 중인 타이머고, 동기화 기록이 없다면 (첫 시작 or 재 시작(경과 시간 반영 O))
                LocalDateTime startAt = timer.getRestartAt() == null ? timer.getStartAt() : timer.getRestartAt();
                long duration = Duration.between(startAt, now).getSeconds();
                if (duration >= 300) { //5분 경과 시
                    timer.updateSyncedAt(); //동기화 시간에 현재 시간 넣기

                    if (duration >= timer.getPlan().getMinutes() * 60) {
                        EventPayload payload = new EventPayload();
                        payload.setType("plan-completed");
                        payload.setId(timer.getUser().getUser_id());
                        payload.setMessage("계획 목표 달성 시간을 채웠습니다! 타이머를 정지 및 종료하면 계획이 완료돼요");
                        sseEmitterService.broadcast(timer.getUser(), payload);
                    }
                }
            }
        }
    }
}

