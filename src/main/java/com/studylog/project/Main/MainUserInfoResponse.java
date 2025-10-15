package com.studylog.project.Main;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MainUserInfoResponse {
    private String nickname;
    private String resolution;
    private long UnreadNotification;
}
