package com.studylog.project.global.event;

import com.studylog.project.timer.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor

// 프론트-웹소켓 연결 끊기면 실행되는 이벤트리스너 (SessionDisconnectEvent 들음)
public class WebSocketEventListener {
    private final TimerService timerService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 연결 끊긴 파이프 고유번호, 유저 정보 얻음
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 기존 JwtAuthenticationFilter가 쿠키를 검증했기 때문에 웹소켓 연결 시에도 유저 정보(Principal)가 자동으로 들어옴
        Principal user = headerAccessor.getUser(); // 끊어진 파이프의 유저 정보 얻음 (웹소켓 연결 때 인증된 객체)

        if (user != null) {
            String userId = user.getName(); // 유저 식별자
            System.out.println("🚨 [웹소켓 끊김 감지] 창 닫힘. 유저: " + userId);

            // 해당 유저가 현재 'RUNNING' 상태인 타이머를 찾아서 전부 'PAUSED'로 변경하는 메서드 호출
            timerService.pauseRunningTimerOnDisconnect(userId);
        }
    }
}