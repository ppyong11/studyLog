package com.studylog.project.sse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;

@NoArgsConstructor //이거 없으면 생성자 직접 만들어야 함
@Getter
@Setter
public class EventPayload {
    private String type;
    private Long id;
    private String message;
}
