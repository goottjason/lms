package com.goott5.lms.user;

import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.service.UserService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {

  @Autowired
  private UserService userService;

  @Test
  public void signupTest() throws IOException {

    SignupDTO signupDTO = SignupDTO.builder()
            .id("7")
            .loginId("ace")
            .password("ace!@34")
            .email("ace@lms.com")
            .mobile("01034567890")
            .address("서울시 노원구 노해로 432")
            .build();

    userService.signup(signupDTO);

  }

  @Test
  public void findUserTest() throws IOException {

    userService.findUserByLoginId("7");

  }

}
