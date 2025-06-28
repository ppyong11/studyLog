package com.studylog.project.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SignInRequest {
    @NotBlank(message = "아이디가 입력되지 않았습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
    private String id;
    @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
    private String pw;
    @NotBlank(message = "닉네임이 입력되지 않았습니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자 한글, 영어, 숫자여야 합니다")
    private String nickname;
    @NotBlank(message = "이메일이 입력되지 않았습니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    //회원가입 DTO to Entity
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .pw(pw)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
