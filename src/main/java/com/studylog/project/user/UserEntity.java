package com.studylog.project.user;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.category.CategoryEntity;
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
    //회원 역할, DB에서 기본값 0으로 처리, 파라미터 안 받음 (빌더 필드에 없어도 알아서 false 들어감 *멤버변수 초기값(boolean= false, Boolean(객체)= null)
    private boolean role;

    @Column //회원탈퇴 여부, DB에서 기본값 0으로 처리, 파라미터 안 받음
    private boolean is_delete;

    @Column //탈퇴 일자
    private LocalDateTime delete_at;

    @OneToMany(mappedBy = "user")
    private List<BoardEntity> boards= new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PlanEntity> plans= new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<CategoryEntity> categories= new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TimerEntity> timers = new ArrayList<>();

    @Builder
    public UserEntity(String id, String pw, String nickname, String email) {
        this.id = id;
        this.pw = pw;
        this.nickname = nickname;
        this.email = email;
        //user_id, role, is_delete, delete_at은 안 다룸
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
        this.is_delete = true;
        this.delete_at = deleteAt;
    }
    //탈퇴 취소
    public void restore(){
        this.is_delete = false;
        this.delete_at = null;
    }
}
