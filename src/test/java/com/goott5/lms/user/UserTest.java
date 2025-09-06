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
        .id(1)
        .loginId("goott5admin")
        .password("admin123")
        .email("goottjason@gmail.com")
        .mobile("01027878712")
        .address("서울시 금천구 시흥대로153길 90-4")
        .build();

    userService.signup(signupDTO);

  }

  @Test
  public void findUserTest() throws IOException {

    userService.findUserByLoginId("7");

  }

}
