package com.studylog.project.mail;

import com.studylog.project.global.exception.MailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    public void sendEmailCode(String email) {
        SimpleMailMessage message = new SimpleMailMessage(); //전송 내용 담는 객체
        if(Boolean.TRUE.equals(redisTemplate.hasKey("verified:" + email))) {
            //이미 검증 완료된 메일
            throw new MailException("이미 검증 완료된 메일입니다. 회원가입을 진행해 주세요.");
        }
        String code= randomCode(); //랜덤 코드 생성
        String text = String.format("""
        안녕하세요, Study Log입니다.
        아래 인증 코드를 입력해 주세요.
        
        ✅ 인증 코드: %s
        인증 코드는 3분간 유효합니다.
        
        ⚠️ 요청하지 않은 경우, 이 메일은 무시하셔도 됩니다.
        """, code);
        try{
            message.setTo(email);
            message.setSubject("[Study Log] 이메일 인증 코드");
            message.setText(text);
            long startTime= System.currentTimeMillis();
            log.info("메일 발송 시작 시간: {}", startTime);
            mailSender.send(message); //구현체로 메일 전송 (실제 SMTP 서버로 전송됨)

            log.info("메일 발송 성공");
            long endTime= System.currentTimeMillis();
            log.info("메일 발송 완료 시간: {}", endTime);
            log.info("메일 발송 소요 시간(ms): " + (endTime-startTime));
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
        redisTemplate.opsForValue().set("send:"+email, code, Duration.ofMinutes(3));
        log.info("redis 인증 코드 저장 완료: "+redisTemplate.opsForValue().get("send:"+email));
    }

    //인증 코드 검증
    public void verifyEmailCode(String email, String code) {
        String storedCode= redisTemplate.opsForValue().get("send:"+email); //코드 값 꺼내기

        if(storedCode == null) throw new MailException("인증 코드가 만료되었습니다."); //TTL 만료 or 이메일 저장 X
        if(!storedCode.equals(code)) throw new MailException("인증 코드가 일치하지 않습니다."); //코드 일치 X

        redisTemplate.opsForValue().set("verified:" + email, "true", Duration.ofMinutes(10)); //검증 완료한 메일 담는 redis
        log.info("인증 및 검증 메일 redis 저장 완료: {}", email);
    }
}