package com.studylog.project.user;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.notification.NotificationEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.timer.TimerEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Table(name="user")
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(nullable = false, unique = true)
    private String id;

    private String pw;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String resolution;

    @Column(nullable = false)
    //회원 역할, DB에서 기본값 0으로 처리, 파라미터 안 받음 (빌더 필드에 없어도 알아서 false 들어감 *멤버변수 초기값(boolean= false, Boolean(객체)= null)
    private Boolean role;

    @Column(name= "is_delete", nullable = false) //회원탈퇴 여부, DB에서 기본값 0으로 처리, 파라미터 안 받음
    private boolean delete; //isIsDelete()로 롬복이 만들어줌 (getter)

    @Column(name= "delete_at") //탈퇴 일자, DB 속성명이랑 달라서 명시 필요
    private LocalDateTime deleteAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<BoardEntity> boards= new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<PlanEntity> plans= new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<CategoryEntity> categories= new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<TimerEntity> timers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<NotificationEntity> notifications= new ArrayList<>();

    @Builder
    public UserEntity(String id, String pw, String nickname, String email) {
        this.id = id;
        this.pw = pw;
        this.nickname = nickname;
        this.email = email;
        this.role = false; //기본값이 null이라 직접 설정
        //user_id, is_delete, delete_at은 안 다룸
        //null 허용 X인 필드(is_delete)는 필드 기본 값 들어가서 null 처리 안 됨
    }

    public void setEncodedPw(String encodedPw) {
        this.pw = encodedPw;
    }

    //수정 가능 필드: 닉네임, 비밀번호
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }
    public void changePw(String newEncodedPw) {
        this.pw = newEncodedPw;
    }

    //회원탈퇴
    public void withdraw(LocalDateTime deleteAt){
        this.delete = true;
        this.deleteAt = deleteAt;
    }
    //탈퇴 취소
    public void restore(){
        this.delete = false;
        this.deleteAt = null;
    }

    //다짐 수정
    public void updateResolution(String resolution){
        this.resolution= resolution;
    }
}
