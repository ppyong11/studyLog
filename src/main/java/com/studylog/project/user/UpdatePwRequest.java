package com.studylog.project.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdatePwRequest {
    @NotBlank
    private String currentPw;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "비밀번호는 6~20자 영문 또는 숫자여야 합니다.")
    private String newPw;
}
