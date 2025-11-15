package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 request")
public record LogInRequest(
        @Schema(description = "유저 id (영문 및 숫자, 4~12자 이내)")
        String id,
        @Schema(description = "유저 pw (영문 및 숫자, 6~20자 이내)")
        String pw
) {}
