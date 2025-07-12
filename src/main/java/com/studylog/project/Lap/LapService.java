package com.studylog.project.Lap;

import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.timer.*;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LapService {
    private final LapRepository lapRepository;
    private final TimerRepository timerRepository;

    //랩 추가는 실행, 정지 상관 X, 랩 실행은 무조건 타이머 실행 상태
    public TimerDetailResponse createLap(Long timerId, LapRequest request, UserEntity user) {
        //timer 영속성 컨텍스트에 관리돼서 lap save 하면 컨텍스트에도 새로운 LapEntity 등록됨
        //랩 엔티티 만들 때 타이머만 잘 들어가면 됨!
        TimerEntity timer= timerRepository.findByUserAndId(user, timerId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타이머입니다."));
        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 타이머는 랩 생성이 불가합니다.");
        //타이머에 동일 랩 네임 X
        if(lapRepository.existsByTimerIdAndLapName(timerId, request.getName().trim()))
            throw new BadRequestException("해당 랩명이 존재합니다.");
        //존재 X 랩명이면
        LapEntity lap= request.toEntity(request, timer);
        lapRepository.save(lap); //id 필드도 다 참
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse updateLap(Long timerId, Long lapId, LapRequest request, UserEntity user) {
        LapEntity lap= getLapByUserAndTimer(timerId, lapId, user);
        if(lapRepository.existsByTimerIdAndLapName(timerId, request.getName().trim())) {
            if(!lap.getLapName().equals(request.getName().trim()))
                throw new BadRequestException("해당 랩명이 존재합니다.");
        }
        lap.updateName(request.getName());
        return TimerDetailResponse.toDto(lap.getTimer());
    }

    public TimerDetailResponse startLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapByUserAndTimer(timerId, lapId, user);
        TimerEntity timer= lap.getTimer();

        if(!timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 아닙니다.");

        //실행 타이머라면 랩 상태 체크
        if(lap.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("이미 실행 중인 랩입니다.");

        if(lapRepository.existsByTimerAndStatus(timer, TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 랩이 있습니다. 정지/종료 후 다시 시도해 주세요.");

        switch (lap.getStatus()) {
            case READY -> lap.startLap();
            case PAUSED -> lap.updateRestartLap();
            case ENDED -> throw new BadRequestException("종료된 랩은 재실행이 불가합니다.");
        }
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse pauseLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapByUserAndTimer(timerId, lapId, user);
        TimerEntity timer= lap.getTimer();

        switch (lap.getStatus()) {
            case RUNNING -> lap.updatePauseLap();
            case ENDED -> throw new BadRequestException("종료된 랩은 재실행이 불가합니다.");
            default -> throw new BadRequestException("실행 중인 랩이 아닙니다.");
        }

        lap.updateElapsed(getTotalElapsed(lap));
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse endLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapByUserAndTimer(timerId, lapId, user);
        TimerEntity timer= lap.getTimer();

        switch (lap.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                lap.updateEndLap(LocalDateTime.now());
                lap.updateElapsed(getTotalElapsed(lap)); //누적 시간 갱신
            }
            case ENDED -> throw new BadRequestException("이미 종료된 랩입니다.");
            case READY -> throw new BadRequestException("실행 중인 랩이 아닙니다.");
            case PAUSED -> lap.updateEndLap(lap.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }

        return TimerDetailResponse.toDto(timer);

    }

    //타이머 전체 기록에 영향 X
    public TimerDetailResponse deleteLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapByUserAndTimer(timerId, lapId, user);
        lapRepository.delete(lap);
        lapRepository.flush(); //delete 쿼리 안 나가서 넣음
        return TimerDetailResponse.toDto(lap.getTimer());
    }

    //초 단위 경과 시간 넘김 (현재 누적 시간 + 이전 누적 시간)
    public Long getTotalElapsed(LapEntity lap) {
        LocalDateTime time= null;
        LocalDateTime startAt;

        if(lap.getSyncedAt() == null){ //동기화 전
            startAt = lap.getRestartAt() == null ? lap.getStartAt() : lap.getRestartAt();
            switch (lap.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= lap.getPauseAt();
                case ENDED -> time= lap.getEndAt();
            }
        } else {
            startAt = lap.getSyncedAt();
            switch (lap.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= lap.getPauseAt();
                case ENDED -> time= lap.getEndAt();
            }
        }

        Duration duration= Duration.between(startAt, time);
        return duration.getSeconds() + lap.getElapsed();
    }

    //타이머 + 유저 검사 -> 타이머에 해당하는 랩인지 검사 (내 특정 타이머에 해당하는 랩이 맞는지 검사)
    private LapEntity getLapByUserAndTimer(Long timerId, Long lapId, UserEntity user) {
        TimerEntity timer= timerRepository.findByUserAndId(user, timerId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타이머입니다."));
        return lapRepository.findByIdAndTimer(lapId, timer)
                .orElseThrow(() -> new NotFoundException("해당 타이머에 존재하지 않는 랩입니다."));
    }
}
