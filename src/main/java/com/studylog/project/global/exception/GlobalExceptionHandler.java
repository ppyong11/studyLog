package com.studylog.project.global.exception;

import com.studylog.project.global.filterException.TokenBlacklistedException;
import com.studylog.project.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, e.getMessage())); //오류 내용 축약해서 출력
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse> handleDuplicate(DuplicateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        //축약 메시지 뽑음
        return ResponseEntity.badRequest().body(new ApiResponse(false, String.join(" / ", messages)));
    }
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse> handleMail(MailException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    }


    @ExceptionHandler(LoginFaildException.class)
    public ResponseEntity<ApiResponse> handleLogFaild(LoginFaildException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    }

    @ExceptionHandler(LogoutFaildException.class)
    public ResponseEntity<ApiResponse> handleLogoutFaild(LogoutFaildException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse> handleInvalidRequest(InvalidRequestException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    }
}
