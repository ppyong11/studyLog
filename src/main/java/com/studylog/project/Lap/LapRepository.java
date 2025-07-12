package com.studylog.project.Lap;

import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LapRepository extends JpaRepository<LapEntity, Long> {
    //Timer 객체 + JPA 네이밍 파싱으로 알아서 id 찾아줌
    boolean existsByTimerIdAndLapName(Long timerId, String name);
    boolean existsByTimerAndStatus(TimerEntity timerEntity, TimerStatus status);

    //실행 중인 랩 가져오기
    Optional<LapEntity> findByTimerAndStatus(TimerEntity timer, TimerStatus status);
    Optional<LapEntity> findByIdAndTimer(Long lapId, TimerEntity timer);

    List<LapEntity> findAllByTimerAndStatus(TimerEntity timer, TimerStatus status);
    List<LapEntity> findAllByTimer(TimerEntity timer);
}
