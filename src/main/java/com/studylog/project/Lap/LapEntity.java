package com.studylog.project.Lap;

import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name="lap")
@Entity
public class LapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "lap_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name= "timer_id", nullable = false)
    private TimerEntity timer;

    @Column(name="lap_name", nullable=false)
    private String lapName;

    @Column(name="create_at", nullable=false)
    private LocalDate createDate;

    @Column(name="start_at")
    private LocalDateTime startAt;
    @Column(name="restart_at")
    private LocalDateTime restartAt;
    @Column(name="pause_at")
    private LocalDateTime pauseAt;
    @Column(name="end_at")
    private LocalDateTime endAt;
    private Long elapsed;
    @Column(name="synced_at")
    private LocalDateTime syncedAt;

    private TimerStatus status;

    @Builder
    public LapEntity (TimerEntity timer, String name){
        this.timer= timer;
        this.lapName= name.trim();
        this.createDate= LocalDate.now();
        this.startAt= null;
        this.restartAt= null;
        this.pauseAt= null;
        this.endAt= null;
        this.syncedAt= null;
        this.elapsed= 0L;
        this.status= TimerStatus.READY;
    }

    public void updateName(String name){
        this.lapName= name.trim();
    }

    public void startLap() {
        this.startAt = LocalDateTime.now();
        this.status = TimerStatus.RUNNING;
    }

    public void updateRestartLap(){
        this.restartAt = LocalDateTime.now();
        status = TimerStatus.RUNNING;
        this.syncedAt = null;
    }

    public void updatePauseLap(){
        this.pauseAt = LocalDateTime.now();
        this.status = TimerStatus.PAUSED;
    }

    public void updateEndLap(LocalDateTime endAt) {
        this.endAt = endAt;
        status = TimerStatus.ENDED;
    }

    public void updateElapsed(Long elapsed) {
        this.elapsed= elapsed;
    }

    //동기화 시간
    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }
}
