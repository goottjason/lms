package com.goott5.lms.coursemanagement;

import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.service.UserService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseManagementTests {
  @Autowired
  private UserService userService;

  @Test
  public void signupTest() throws IOException {

    SignupDTO signupDTO = SignupDTO.builder()
        .id(7)
        .loginId("queen1")
        .password("queen!@34")
        .email("ji123@abc.com")
        .mobile("01083839595")
        .address("서울시 노원구 노해로 432")
        .build();

    userService.signup(signupDTO);

  }

  @Test
  public void findUserTest() throws IOException {

    userService.findUserByLoginId("7");

  }

}