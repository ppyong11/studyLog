package com.studylog.project.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record IdRequest(
        // 패턴이 있어도 아무것도 입력 안 했을 땐 이 메시지 날리는 게 맞음 (빈 값 확인)
        @NotBlank(message = "아이디가 입력되지 않았습니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
        String id
)
{
    // 오버라이드
    public IdRequest {
        id = id.trim();
    }
}
