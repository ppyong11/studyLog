package com.studylog.project.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SignInRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
    private String id;
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
    private String pw;
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자 한글, 영어, 숫자여야 합니다")
    private String nickname;
    @NotBlank(message = "이메일은 필수입니다.")
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

    //닉네임, 비밀번호 수정 메소드
    public void applyTo(UserEntity user) {
        if (nickname != null && !nickname.equals(user.getNickname())) {
            user.changeNickname(nickname);
        }
        if (pw != null && !pw.equals(user.getPw())) {
            user.changePw(email);
        }
    }
}
