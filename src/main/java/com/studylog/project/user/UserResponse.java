package com.studylog.project.user;

public record UserResponse(Long id, String userId, String nickname, String email, String role) {
    public static UserResponse of(UserEntity user) {
        String role = user.getRole()? "ADMIN" : "USER";

        return new UserResponse(user.getUser_id(), user.getId(), user.getNickname(), user.getEmail(), role);
    }
}
