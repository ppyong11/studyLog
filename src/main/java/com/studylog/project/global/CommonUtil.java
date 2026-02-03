package com.studylog.project.global;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommonUtil {

    private CommonUtil() {}; // 인스턴스 생성 방지

    //카테고리 조회 검증 로직 (static이라 다른 파일에서 객체 선언 없이 사용 O)
    public static List<Long> parseAndValidateCategory(String category) {
        List<Long> categoryList = new ArrayList<>();

        //카테고리가 null이고
        if (category == null || category.trim().isEmpty()) {
            return categoryList;
        }


        if(!category.matches("^[0-9,]+$")) {
            log.info("잘못된 입력 값 - {}, 숫자와 콤마만 입력 가능", category);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if(!category.contains(",") && category.contains(" ")){
            //category=1  2 (거름)
            throw new CustomException(ErrorCode.CATEGORY_SEPARATED);
        }

        try{
            categoryList= Arrays.stream(category.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch(NumberFormatException e){
            log.info("잘못된 입력 값 - {}, 숫자와 콤마만 입력 가능", category);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return categoryList;
    }
}
