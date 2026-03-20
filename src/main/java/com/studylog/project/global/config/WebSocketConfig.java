package com.studylog.project.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트엔드에서 접속할 엔드포인트 주소: ws://localhost:8080/api/ws-timer
        registry.addEndpoint("/api/ws-timer")
                .setAllowedOriginPatterns("http://localhost:3000", "https://studylog.hyeoncode.dev",
                        "https://api.studylog.hyeoncode.dev") // CORS 허용
                .withSockJS(); // 브라우저 호환성을 위한 SockJS 사용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
