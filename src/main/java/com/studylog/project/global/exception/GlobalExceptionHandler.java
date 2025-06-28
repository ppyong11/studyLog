package com.studylog.project.global.exception;

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
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage())); //오류 내용 축약해서 출력
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse> handleDuplicate(DuplicateException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        //축약 메시지 뽑음
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(new ApiResponse(httpStatus.value(), false, String.join(" / ", messages)));
    }
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse> handleMail(MailException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }


    @ExceptionHandler(LoginFaildException.class)
    public ResponseEntity<ApiResponse> handleLogFaild(LoginFaildException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(LogoutFaildException.class)
    public ResponseEntity<ApiResponse> handleLogoutFaild(LogoutFaildException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse> handleInvalidRequest(InvalidRequestException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(JwtException.class) //jwt 관련 exception
    public ResponseEntity<ApiResponse> handleJwtException(JwtException e) {
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(AlreadyDeleteUserException.class)
    public ResponseEntity<ApiResponse> handleAlreadyDeleteUser(AlreadyDeleteUserException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleSignup(BadRequestException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse(httpStatus.value(), false, e.getMessage()));
    }
}
