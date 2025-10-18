package com.studylog.project.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfoResponse {
    private String nickname;
    private String resolution;
    private long UnreadNotification;
}
