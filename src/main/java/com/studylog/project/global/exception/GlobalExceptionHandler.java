package com.studylog.project.global.exception;

import com.studylog.project.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(500)
                .body(new CommonResponse(false, e.getMessage())); //오류 내용 축약해서 출력
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<CommonResponse> handleDuplicate(DuplicateException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        //축약 메시지 뽑음
        return ResponseEntity.status(400).body(new CommonResponse(false, String.join(" / ", messages)));
    }
    @ExceptionHandler(MailException.class)
    public ResponseEntity<CommonResponse> handleMail(MailException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse( false, e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, "파일 용량 제한 20MB를 넘어 업로드할 수 없습니다."));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<CommonResponse> handleFileUpload(FileUploadException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }
    @ExceptionHandler(LoginFaildException.class)
    public ResponseEntity<CommonResponse> handleLogFaild(LoginFaildException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(LogoutFaildException.class)
    public ResponseEntity<CommonResponse> handleLogoutFaild(LogoutFaildException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonResponse> handleInvalidRequest(InvalidRequestException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(JwtException.class) //jwt 관련 exception
    public ResponseEntity<CommonResponse> handleJwtException(JwtException e) {
        return ResponseEntity.status(401)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AlreadyDeleteUserException.class)
    public ResponseEntity<CommonResponse> handleAlreadyDeleteUser(AlreadyDeleteUserException e) {
        return ResponseEntity.status(409)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonResponse> handleSignup(BadRequestException e) {
        return ResponseEntity.status(400)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(404)
                .body(new CommonResponse(false, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403)
                .body(new CommonResponse(false, e.getMessage()));
    }
}
