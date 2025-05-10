package com.studylog.project.dto.request;

import com.studylog.project.entity.UserEntity;
import lombok.Getter;

@Getter
public class UserRequest {
    private String id;
    private String pw;
    private String nickname;
    private String email;

    //회원가입 DTO to Entity
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .pw(pw)
                .nickname(nickname)
                .email(email)
                .build();
    }

    //닉네임, 비밀번호 수정 메소드
    public void applyTo(UserEntity user) {
        if (nickname != null && !nickname.equals(user.getNickname())) {
            user.changeNickname(nickname);
        }
        if (pw != null && !pw.equals(user.getPw())) {
            user.changePw(email);
        }
    }
}
