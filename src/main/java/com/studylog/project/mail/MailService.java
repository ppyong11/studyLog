package com.studylog.project.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    //이메일 전송 API
    private final JavaMailSender mailSender; //yml 기반 Bean 생성, @Required~가 빈 등록 완료
    private final RedisTemplate<String, String> redisTemplate; //제네릭 타입 명시

    public void sendEmailCode(String email) {
        SimpleMailMessage message = new SimpleMailMessage(); //전송 내용 담는 객체

        String code= randomCode(); //랜덤 코드 생성

        try{
            message.setTo(email);
            message.setSubject("Study Log 이메일 인증 코드입니다.");
            message.setText("인증 코드: "+code+"\n인증 코드는 2분 동안 유효합니다.");
            mailSender.send(message); //구현체로 메일 전송 (실제 SMTP 서버로 전송됨)

            log.info("메일 발송 성공");
            saveCode(email, code); //코드 저장
        } catch (Exception e){ //체크, 언체크 예외 처리
            log.error("메일 발송 실패 -"+e.getMessage()); //에러 내용 반환
            throw new RuntimeException(e.getMessage(), e); //unchecked 예외 던짐 (핸들러가 받음)
        }
    }
    //인증 코드 생성
    public String randomCode() {
        StringBuilder code= new StringBuilder(); //문자열 합치는 거 빠르게
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        for (int i= 0; i < 6; i++) {
            code.append(random.nextInt(10)); //0~9까지의 무작위 int 값 리턴
        }
        return code.toString();
    }

    //인증 코드 저장
    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set("email:"+email, code, Duration.ofMinutes(2));
        log.info("redis 인증 코드 저장 완료: "+redisTemplate.opsForValue().get("email:"+email));
    }

    //인증 코드 검증
    public MailErrorCode verifyEmailCode(String email, String code) {
        String storedCode= redisTemplate.opsForValue().get("email:"+email);

        if(storedCode == null) return MailErrorCode.CODE_EXPIRED; //TTL 만료 or 이메일 저장 X
        if(!storedCode.equals(code)) return MailErrorCode.CODE_EXPIRED; //코드 일치 X

        return MailErrorCode.SUCCESS; //위 상황 제외면 인증 처리
    }

}
