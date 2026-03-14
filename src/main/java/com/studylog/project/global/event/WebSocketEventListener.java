package com.studylog.project.global.event;

import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.timer.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
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
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();

        // 1. user가 null이 아니고 Authentication 타입인지 확인 (안전성 검사)
        if (user instanceof Authentication authentication) {

            // 2. Authentication 안에 들어있는 객체 꺼내기
            CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();

            Long userPk = customUserDetail.getUserId();

            System.out.println("🚨 [웹소켓 끊김 감지] 창 닫힘. 유저 PK: " + userPk);

            timerService.pauseRunningTimerOnDisconnect(userPk);
        }
    }
}