package com.studylog.project.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String nickname;
    private String role;

    public static UserResponse toDto(UserEntity user) {
        String role = user.getRole()? "ADMIN" : "USER";
        return new UserResponse(user.getUser_id(), user.getNickname(), role);
    }
}
