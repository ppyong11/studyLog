package com.studylog.project.notification;

import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity(name = "notification")
@Table
@Getter
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "noti_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="timer_id")
    private TimerEntity timer;

    private String title;
    private String content;

    @Column(name = "alert_at")
    private LocalDateTime alertAt;
    private String url;

    @Column(name = "is_read")
    private boolean isRead;
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Builder
    public NotificationEntity(UserEntity user, TimerEntity timer, String title, String content, String url){
        this.user= user;
        this.timer= timer;
        this.title= title;
        this.content= content;
        this.alertAt= LocalDateTime.now();
        this.url= url;
        this.isRead= false;
        this.isDeleted= false;
    }

    public void updateIsRead(){
        this.isRead= true;
    }
    public void deletedTimer(){
        this.url= null;
        this.isDeleted= true;
    }
}
