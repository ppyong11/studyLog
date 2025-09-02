package com.studylog.project.Lap;

import com.studylog.project.global.domain.TimerLapBaseEntity;
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
public class LapEntity extends TimerLapBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "lap_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name= "timer_id", nullable = false)
    private TimerEntity timer;

    @Builder
    public LapEntity (TimerEntity timer, String name){
        this.timer= timer;
        this.name= name.trim();
        this.createDate= LocalDate.now();
        this.startAt= null;
        this.pauseAt= null;
        this.endAt= null;
        this.syncedAt= null;
        this.elapsed= 0L;
        this.status= TimerStatus.READY;
    }
}
