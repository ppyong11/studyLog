package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(description = "로그인 request")
public class LogInRequest {
    @Schema(description = "유저 id (영문 및 숫자, 4~12자 이내)")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
    private String id;
    @Schema(description = "유저 pw (영문 및 숫자, 6~20자 이내)")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
    private String pw;
}
