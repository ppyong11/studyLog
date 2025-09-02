package com.studylog.project.global.domain;

import com.studylog.project.timer.TimerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor
@Getter
public abstract class TimerLapBaseEntity {
    @Column(nullable = false)
    protected String name;

    @Column(name="create_date")
    protected LocalDate createDate;

    @Column(name= "start_at")
    protected LocalDateTime startAt;

    @Column(name= "pause_at")
    protected LocalDateTime pauseAt;

    @Column(name= "end_at")
    protected LocalDateTime endAt;

    @Column(name="elapsed", nullable = false)
    protected Long elapsed;

    @Column(name="synced_at")
    protected LocalDateTime syncedAt;

    @Column(length = 10, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    protected TimerStatus status;

    //메서드는 서비스에서 실행하니까 public
    //타이머, 랩명 업데이트
    public void updateName(String name) {
        this.name= name.trim();
    }

    public void start(){
        this.startAt = LocalDateTime.now();
        this.status = TimerStatus.RUNNING;
        this.syncedAt = null;
    }

    public void pause(){
        this.pauseAt = LocalDateTime.now();
        this.status = TimerStatus.PAUSED;
    }

    public void end(LocalDateTime endAt){
        this.endAt = endAt;
        status = TimerStatus.ENDED;
    }

    //리셋
    public void reset(){
        this.startAt= null;
        this.pauseAt= null;
        this.endAt= null;
        this.elapsed= 0L;
        this.syncedAt= null;
        this.status= TimerStatus.READY;
    }

    //누적 시간
    public void updateElapsed(Long elapsed){
        this.elapsed= elapsed;
    }

    //동기화 시간
    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }
}
