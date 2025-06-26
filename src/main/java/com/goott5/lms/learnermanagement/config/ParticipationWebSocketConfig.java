/*

package com.goott5.lms.learnermanagement.config;

import lombok.RequiredArgsConstructor; // final 필드에 대한 생성자 자동 생성
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration; // 스프링 설정 클래스임을 명시
import org.springframework.web.socket.WebSocketHandler; // 웹소켓 핸들러 인터페이스
import org.springframework.web.socket.config.annotation.EnableWebSocket; // 웹소켓 활성화 어노테이션
import org.springframework.web.socket.config.annotation.WebSocketConfigurer; // 웹소켓 설정 인터페이스
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry; // 웹소켓 핸들러 등록

@Configuration // 스프링 설정 클래스임을 나타냄
@EnableWebSocket // 웹소켓 기능 활성화
@RequiredArgsConstructor // final 필드(webSocketHandler)에 대한 생성자 자동 생성
@Qualifier
public class ParticipationWebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler; // 웹소켓 핸들러 주입

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // 웹소켓 핸들러 등록 메서드
        registry.addHandler(webSocketHandler, "/participationSocket")
            .setAllowedOrigins("http://localhost:8090"); // /socketTest 경로에 핸들러 등록, 모든 출처 허용
    }
}
*/
