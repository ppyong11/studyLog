package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(description = "다짐 설정 request (30자 이내)")
public class UpdateResolutionReqeust {
    @Size(max= 30, message = "30자 이내로 입력해 주세요.")
    private String resolution;
}
