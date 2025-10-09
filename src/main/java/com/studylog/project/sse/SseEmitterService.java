package com.studylog.project.sse;

import com.studylog.project.notification.NotificationService;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SseEmitterService {
    //SSE 이벤트 타임아웃 시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; //1시간
    private final EmitterRepository emitterRepository;
    private final NotificationService notificationService;

    //유저가 구독 조회
    public boolean isSubscribe(UserEntity user) {
        return emitterRepository.existsByUser(user.getUser_id());
    }
    //관리자가 전체 구독 유저 조회

    /*클라이언트의 이벤트 구독을 허용하는 메서드
      최근에 추가한 emitter에만 구독 확인용 메시지 보내기*/
    public SseEmitter subscribe(UserEntity user){
        //sse의 유효 시간이 만료되면 클라에서 다시 서버로 이벤트 구독을 시도함
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(user.getUser_id(), emitter);

        /*이벤트 콜백 등록
          사용자가 탭 닫을 때, 모든 알림 전송 시, 유효 시간 만료 시 삭제*/
        emitter.onCompletion(()-> emitterRepository.deleteByUserId(user.getUser_id(), emitter));
        emitter.onTimeout(()-> emitterRepository.deleteByUserId(user.getUser_id(), emitter));

        //첫 구독 시에 이벤트 발생
        try{
            emitter.send(
                    SseEmitter.event()
                            .id(UUID.randomUUID().toString())
                            .name("subscribe-event")
                            .data("subscribe-event, id:" + user.getUser_id())
            );
        } catch (IOException e) {
            throw new RuntimeException("SSE 연결 실패"); //구독은 된 것
        }
        return emitter;
    }

    //이벤트 구독되어 있는 유저에게 알림 전송
    public void broadcast(UserEntity user, EventPayload payload){
        //알림 받을 유저
        List<SseEmitter> emitters = emitterRepository.findByUserId(user.getUser_id());
        //이벤트 식별자
        for (SseEmitter emitter : emitters)
            try{
                emitter.send(
                        SseEmitter.event()
                                .id(UUID.randomUUID().toString()) //이벤트 식별자 (프론트에서 어디까지 받았는지 기록)
                                .name(payload.getType())
                                .data(payload) //json 변환
                );
            } catch (IOException e) {
                emitterRepository.deleteByUserId(user.getUser_id(), emitter); //연결 오류 emitter 삭제
                log.warn("에러 emitter 삭제"); //다음 emitter 진행
            }
    }

    public void alert(TimerEntity timer, UserEntity user, boolean isSyncCheck){
        notificationService.saveNotification(user, timer, isSyncCheck);

        EventPayload payload = new EventPayload();
        payload.setType("plan-completed");
        payload.setId(timer.getUser().getUser_id());
        payload.setTitle(String.format("[%s] 계획이 %s완료 처리되었어요. 🥳",
                timer.getPlan().getPlan_name(), isSyncCheck? "자동":""));

        broadcast(timer.getUser(), payload);
    }
}
