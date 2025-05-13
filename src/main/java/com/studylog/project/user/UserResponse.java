package com.studylog.project.user;

import lombok.Getter;

@Getter
public class UserResponse {
    private String id;
    private String nickname;
    private String email;

    public UserResponse(UserEntity user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
    }
}
