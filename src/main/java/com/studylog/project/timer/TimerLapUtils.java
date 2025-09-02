package com.studylog.project.timer;

import com.studylog.project.global.domain.TimerLapBaseEntity;

import java.time.Duration;
import java.time.LocalDateTime;

//순수 자바 클래스로 두기 (스프링 빈으로 사용할 시, @Component 붙여서 서비스처럼 활용)
public class TimerLapUtils {
    private TimerLapUtils(){} //인스턴스 생성 방지

    //초 단위 경과 시간 넘김 (현재 누적 시간 + 이전 누적 시간)
    //BaseEntity를 상속한 자식 엔티티만 받음 (추상클래스의 필드, 메서드만 활용 가능)
    public static Long getTotalElapsed(TimerLapBaseEntity entity) {
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
}
