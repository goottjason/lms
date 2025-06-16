package com.goott5.lms.user.service;

import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.mail.MessagingException;

public interface UserService {

  UserVO findUserByLoginId(String loginId);

  UserVO findUserByEmail(String email);

  UserVO findUserByMobile(String mobile);

  UserVO login(LoginDTO loginDTO);

  int saveAutoLogin(int userId, String sessionId, LocalDateTime localDateTime);

  String sendAuthCodeForSignup(String email) throws MessagingException;

  boolean signup(SignupDTO signupDTO) throws IOException;
}
