package com.studylog.project.user;

public record UserResponse(Long id, String nickname, String role) {
    public static UserResponse of(UserEntity user) {
        String role = user.getRole()? "ADMIN" : "USER";

        return new UserResponse(user.getUser_id(), user.getNickname(), role);
    }
}
