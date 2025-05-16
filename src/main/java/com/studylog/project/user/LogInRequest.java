package com.studylog.project.user;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LogInRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]{4,12}$", message = "아이디는 4~12자 영문 또는 숫자여야 합니다.")
    private String id;
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
    private String pw;
}
