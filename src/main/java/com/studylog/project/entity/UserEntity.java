package com.studylog.project.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Table(name="user")
@Entity

public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) //파라미터 안 받음 (setId() 차단)
    private Long user_id;

    @Column(nullable = false, unique = true)
    private String id;

    private String pw;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(insertable = false) //회원 역할, DB에서 기본값 0으로 처리, 파라미터 안 받음
    private Boolean role;

    @Builder
    public UserEntity(String id, String pw, String nickname, String email) {
        this.id = id;
        this.pw = pw;
        this.nickname = nickname;
        this.email = email;
        //user_id, role은 db에서 빌더에서 안 다룸
    }
}
