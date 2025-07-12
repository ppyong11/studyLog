package com.studylog.project.Lap;

import com.studylog.project.timer.TimerEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LapRequest {
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
