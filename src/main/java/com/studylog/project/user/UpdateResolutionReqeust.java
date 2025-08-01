package com.studylog.project.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateResolutionReqeust {
    @Size(max= 30, message = "30자 이내로 입력해 주세요.")
    private String resolution;
}
