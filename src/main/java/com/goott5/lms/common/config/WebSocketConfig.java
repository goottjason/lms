package com.goott5.lms.common.config;

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

    registry.addEndpoint("/ws") // 클라이언트 연결 주소
        .setAllowedOrigins("http://localhost:8090")
        .withSockJS(); // SockJS fallback 사용
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {

    registry.enableSimpleBroker("/topic"); // 구독 주소(prefix) => 구독자는 이걸로 받음
    registry.setApplicationDestinationPrefixes("/app"); // 메시지 발행 주소 (prefix) => 클라이언트는 이걸로 보냄
  }

}
