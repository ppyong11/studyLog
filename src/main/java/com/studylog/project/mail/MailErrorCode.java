package com.studylog.project.mail;

public enum MailErrorCode {
    SUCCESS,
    CODE_EXPIRED, //TTL 만료 및 저장된 이메일이 아님
    MISMATCH
}
