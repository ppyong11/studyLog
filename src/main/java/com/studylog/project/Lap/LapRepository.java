package com.studylog.project.Lap;

import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerStatus;
import com.studylog.project.user.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LapRepository extends JpaRepository<LapEntity, Long> {
    //Timer 객체 + JPA 네이밍 파싱으로 알아서 id 찾아줌
    boolean existsByTimerIdAndName(Long timerId, String name);
    boolean existsByTimerAndStatus(TimerEntity timerEntity, TimerStatus status);

    //실행 중인 랩 가져오기
    Optional<LapEntity> findByTimerAndStatus(TimerEntity timer, TimerStatus status);

    List<LapEntity> findAllByTimer(TimerEntity timer);

    //LapEntity와 연관된 Timer를 fetch join으로 함께 조회하고,
    //LapEntity.id와 타이머의 id와 user가 주어진 값과 일치하는 단일 LapEntity 반환
    @Query("SELECT l from LapEntity l JOIN FETCH l.timer t WHERE l.id= :lapId AND  l.timer.user= :user AND t.id= :timerId")
    Optional<LapEntity> getLapWithTimer(@Param("lapId") Long lapId,
                              @Param("user")UserEntity user,
                              @Param("timerId") Long timerId);
}
