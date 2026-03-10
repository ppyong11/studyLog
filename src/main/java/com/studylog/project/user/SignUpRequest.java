package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "회원가입 reqeust")
public record SignUpRequest(
        @Schema(description = "유저 id (영문 및 숫자, 4~12자 이내)")
        @NotBlank(message = "아이디가 입력되지 않았습니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
        String id,

        @Schema(description = "유저 pw (영문 및 숫자, 6~20자 이내)")
        @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
        String password,

        @Schema(description = "유저 닉네임 (특수 문자 X, 2~10자 이내)")
        @NotBlank(message = "닉네임이 입력되지 않았습니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자 한글, 영어, 숫자여야 합니다")
        String nickname,

        @Schema(description = "유저 이메일")
        @NotBlank(message = "이메일이 입력되지 않았습니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email
) {
    public SignUpRequest {
        id = id.trim();
        password = password.trim();
        nickname = nickname.trim();
        email = email.trim();
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .pw(password)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
