package com.studylog.project.Main;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MainPlansResponse<T> {
    private T plans;
}
