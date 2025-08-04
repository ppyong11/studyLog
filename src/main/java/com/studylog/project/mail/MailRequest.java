package com.studylog.project.mail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "이메일 발송/인증 request")
public class MailRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    @Schema(description = "이메일 인증 코드 입력 (인증 코드 발송 시 필요 X)")
    private String code; //이메일 인증 코드
}
