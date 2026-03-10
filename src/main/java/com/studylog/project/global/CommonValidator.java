package com.studylog.project.global;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class CommonValidator {
    private CommonValidator() {};

    public static void validatePage(int page) {
        if (page < 1) {
            CommonThrow.invalidRequest("잘못된 페이지 값: " + page);
        }
    }

    public static void validateSort(List<String> sort, int sortSize) {
        if (sort.size() != sortSize) {
            CommonThrow.invalidRequest("잘못된 정렬 값: " + sort);
        }
    }

    public static void validateDraftId(String draftId) {
        if (draftId == null || draftId.trim().isBlank()) {
            CommonThrow.invalidRequest("잘못된 게시글 임시 값: " + draftId);
        }
    }

    public static void validateDate(LocalDate startDate, LocalDate endDate) {
        if(startDate == null || endDate == null){
            throw new CustomException(ErrorCode.DATE_RANGE_REQUIRED);
        }
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

}
