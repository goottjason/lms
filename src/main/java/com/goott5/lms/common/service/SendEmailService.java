package com.goott5.lms.common.service;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendEmailService {

  @Value("${mail.username}")
  private String username;
  @Value("${mail.password}")
  private String password;

  private static final String SENDER = "junkyu83@gmail.com";

  private static final Properties props = new Properties();

  static {

    // 지메일 세팅
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    props.put("mail.smtp.auth", "true");
  }

  public void sendEmail(String email, String title, String html) throws MessagingException {

    Session mailSession = Session.getInstance(props, new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    });

    if (mailSession != null) {

      MimeMessage mime = new MimeMessage(mailSession);

      mime.setFrom(new InternetAddress(SENDER)); // 보내는 사람의 메일 주소
      mime.addRecipient(RecipientType.TO, new InternetAddress(email)); // 받는 사람의 메일 주소
      mime.setSubject(title); // 메일 제목
      mime.setText(html, "utf-8", "html");

      Transport.send(mime);
    }
  }
}
