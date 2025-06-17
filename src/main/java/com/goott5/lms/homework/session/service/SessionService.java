package com.goott5.lms.homework.session.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SessionService {

  // 세션 url과 세션 쿼리 스트링 저장
  public String  saveSession(HttpServletRequest request, String sessionUrl) {


    String sessionQueryString = request.getQueryString();

    log.info("sessionQueryString:{}",sessionQueryString);

    // 쿼리스트링이 있을 경우 결합
    if(sessionQueryString != null) {
      sessionUrl += "?" + sessionQueryString;
    }

    request.getSession().setAttribute("sessionSaveUrl",sessionUrl); //세션에 저장
    log.info("sessionUrl 저장 성공:{}",request.getSession().getAttribute("sessionSaveUrl"));

    return sessionUrl;
  }

}
