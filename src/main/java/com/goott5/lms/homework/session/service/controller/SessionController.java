package com.goott5.lms.homework.session.service.controller;

import com.goott5.lms.homework.session.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

  private final SessionService sessionService;

  @PostMapping("/savePage")
  public ResponseEntity<String> session(@RequestBody Map<String,String> savePage, HttpServletRequest request) {

    String savePageStr = savePage.get("savePage"); //프론트에서 보낸 값
    if(savePageStr == null || savePageStr.trim().equals("")) {
        return ResponseEntity.badRequest().body("savePage is null");

    }

    String saveUrl = sessionService.saveSession(request, savePageStr);
    // 세션에 저장된 url
    //(세션에서 현재 url를 볼 수 없으면, 프론트에서 보낸 url로 대체해서 처리)

    if(saveUrl == null || saveUrl.trim().isEmpty()) {
      if(saveUrl.trim().equals("")) {
        return ResponseEntity.badRequest().body("saveUrl is null");
      }
    }

//    log.info("saveUrl: {}", saveUrl);

    return ResponseEntity.ok(saveUrl);
  }

//  @GetMapping("/savePage")
//  public ResponseEntity<String> getSession(HttpServletRequest request) {
//    String saveUrl = (String) request.getSession().getAttribute("sessionSaveUrl");
//
//    return ResponseEntity.ok(saveUrl);
//  }


}
