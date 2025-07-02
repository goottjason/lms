package com.goott5.lms.learnermanagement.controller;

import com.goott5.lms.common.service.SendEmailService;
import com.goott5.lms.learnermanagement.domain.sendEmail.EmailRequestDTO;
import com.goott5.lms.user.domain.ApiResponse;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SendEmailController {

  private final SendEmailService sendEmailService;

  @GetMapping("/learnerManagement/sendEmail")
  public String sendEmailView() {
    return "learnerManagement/sendEmail";
  }

  @PostMapping("/learnerManagement/sendEmail")
  public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody EmailRequestDTO request) {

    log.info("Send email content: {}", request.getContents());

    String html = "<h2>안녕하세요. Goott5 LMS입니다.</h2>";
    html += "<p style='white-space: pre-wrap;'>" + request.getContents() + "</p>";

    try {
      for (String email : request.getEmailList()) {
        log.info("Sending email: " + email);
        sendEmailService.sendEmail(email, request.getTitle(), html);

      }
      return ApiResponse.respondOk(200, "OK", "이메일 발송 완료");
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }
}
