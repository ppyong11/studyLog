package com.studylog.project.Lap;

import com.studylog.project.timer.TimerEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(description = "랩 등록/수정 request")
public class LapRequest {
    @Schema(description = "랩명 (20자 이내)")
    @NotBlank(message = "랩 제목을 입력해 주세요.")
    @Size(max= 20, message = "20자 이내로 입력해 주세요.")
    private String name;

    public LapEntity toEntity(LapRequest request, TimerEntity timer) {
        return LapEntity.builder()
                .name(request.getName().trim())
                .timer(timer)
                .build();
    }
}
