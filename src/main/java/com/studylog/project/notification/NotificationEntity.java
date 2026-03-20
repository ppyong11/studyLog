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
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String title;
    private String content;

    @Column(name = "alert_at")
    private LocalDateTime alertAt;
    @Column(name = "is_read")
    private boolean isRead;

    @Builder
    public NotificationEntity(UserEntity user, String title, String content, String url){
        this.user= user;
        this.title= title;
        this.content= content;
        this.alertAt= LocalDateTime.now();
        this.isRead= false;
    }

    public void updateIsRead(){
        this.isRead= true;
    }
}
