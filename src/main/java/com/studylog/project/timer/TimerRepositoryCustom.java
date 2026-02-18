package com.studylog.project.timer;

import com.studylog.project.user.UserEntity;

import java.time.LocalDate;
import java.util.List;

public interface TimerRepositoryCustom {
    List<TimerResponse> searchTimersByFilter(UserEntity user, LocalDate startDate, LocalDate endDate,
                                             List<Long> categoryList, String planKeyword, String keyword, String status, List<String> sort,
                                             int page);
    Long getTotalItems(UserEntity user, LocalDate startDate, LocalDate endDate,
                    List<Long> categoryList, String planKeyword, String keyword, String status);
}
