package com.goott5.lms.user.service;

import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;

public interface UserService {

  UserVO findUser(String loginId);

  UserVO login(LoginDTO loginDTO);

  boolean signup(@Valid SignupDTO signupDTO) throws IOException;

  int saveAutoLogin(String userId, String sessionId, LocalDateTime localDateTime);

}
