package com.studylog.project.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "다짐 설정 request (30자 이내)")
public record UpdateResolutionReqeust(
        @Size(max= 30, message = "30자 이내로 입력해 주세요.")
        String resolution
) {
    public UpdateResolutionReqeust {
        resolution = resolution.trim();
    }
}
