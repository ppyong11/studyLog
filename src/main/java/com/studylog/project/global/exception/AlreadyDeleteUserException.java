package com.studylog.project.global.exception;

public class AlreadyDeleteUserException extends RuntimeException {
    public AlreadyDeleteUserException(String message) {
        super(message);
    }
}
