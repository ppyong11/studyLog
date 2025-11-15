package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 변경 request (영문 및 숫자, 6~20자 이내)")
public record UpdatePwRequest(
        @NotBlank
        String currentPw,

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
        String newPw
) {}
