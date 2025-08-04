package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(description = "닉네임 변경 request (특수 문자 X, 2~10자 이내)")
public class UpdateNicknameRequest {
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자 한글, 영어, 숫자여야 합니다")
    private String nickname;
}
