package com.studylog.project.global.exception;

//파라미터 누락, 비즈니스 로직 위반 (비밀번호 불일치, 중복 데이터)
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
