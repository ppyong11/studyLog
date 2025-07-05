package com.studylog.project.global;

import com.studylog.project.global.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommonUtil {

    //카테고리 조회 검증 로직 (static이라 다른 파일에서 객체 선언 없이 사용 O)
    public static List<Long> parseAndValidateCategory(String category) {
        if(!category.contains(",") && category.contains(" ")){
            //category=1  2 (거름)
            throw new BadRequestException("카테고리는 콤마(,)로 구분되어야 합니다.");
        }
        //카테고리가 null이 아니고 공백이 아닌 값일 때
        List<Long> categoryList;
        try{
            categoryList= Arrays.stream(category.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch(NumberFormatException e){
            throw new BadRequestException("카테고리는 콤마(,)로 구분되어야 합니다.");
        }
        return categoryList;
    }
}
