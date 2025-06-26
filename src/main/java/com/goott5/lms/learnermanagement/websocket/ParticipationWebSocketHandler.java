/*

package com.goott5.lms.learnermanagement.websocket;


import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component; // 스프링 컴포넌트 어노테이션
import org.springframework.web.socket.CloseStatus; // 웹소켓 종료 상태
import org.springframework.web.socket.TextMessage; // 텍스트 메시지
import org.springframework.web.socket.WebSocketSession; // 웹소켓 세션
import org.springframework.web.socket.handler.TextWebSocketHandler; // 텍스트 메시지 처리 핸들러

import java.io.IOException; // 예외 처리
import java.util.concurrent.ConcurrentHashMap; // 동시성 해시맵
@Primary
@Component // 스프링 빈으로 등록
@Slf4j // 로깅 기능 활성화
public class ParticipationWebSocketHandler extends TextWebSocketHandler { // 텍스트 웹소켓 핸들러 상속
    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>();

//    클라이언트가 연결되면 세션을 저장합니다.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception { // 연결이 성립되었을 때 호출
        CLIENTS.put(session.getId(), session); // 세션 ID와 세션 저장
        log.info("id : {}", session.getAttributes()); // 세션 속성 로그 출력
    }

//    클라이언트가 연결을 종료하면 세션을 제거합니다.
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {// 연결이 종료되었을 때 호출
        CLIENTS.remove(session.getId()); // 세션 제거
        System.out.println("언제끊길까?"); // 종료 로그 출력
    }

    // 클라이언트가 보낸 메시지를 다른 모든 클라이언트에게 전달합니다.
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception { // 텍스트 메시지 수신 처리
        String id = session.getId();  //메시지를 보낸 아이디 // 메시지를 보낸 세션 ID
        CLIENTS.entrySet().forEach( arg->{ // 모든 클라이언트 세션 순회
            if(!arg.getKey().equals(id)) {  //같은 아이디가 아니면 메시지를 전달합니다. // 메시지 보낸 세션 제외
                try {
                    arg.getValue().sendMessage(message); // 다른 클라이언트에 메시지2 전송
                } catch (IOException e) {
                    e.printStackTrace(); // 예외 처리
                }
            }
        });
    }
}
*/
