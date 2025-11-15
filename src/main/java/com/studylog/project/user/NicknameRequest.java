package com.studylog.project.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NicknameRequest(
        @NotBlank(message = "닉네임이 입력되지 않았습니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자 한글, 영어, 숫자여야 합니다")
        String nickname
) {
    public NicknameRequest {
        nickname = nickname.trim();
    }
}
