package com.studylog.project.user;

public record UserInfoResponse(
        String nickname,
        String resolution,
        long UnreadNotification
) {}
