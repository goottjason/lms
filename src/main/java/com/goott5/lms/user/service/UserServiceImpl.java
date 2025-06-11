package com.goott5.lms.user.service;

import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.mapper.UserMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public UserVO findUser(String loginId) {

    return userMapper.selectUserByLoginId(loginId);
  }

  @Override
  public UserVO login(LoginDTO loginDTO) {

    UserVO userVO = userMapper.selectUserByLoginId(loginDTO.getLoginId());

    if (userVO != null) {
      if (passwordEncoder.matches(loginDTO.getPassword(), userVO.getPassword())) {
        return userVO;
      } else {
        userMapper.updateUserWrongPasswordCount(userVO.getId());
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public boolean signup(SignupDTO signupDTO) throws IOException {

    String encryptedPwd = passwordEncoder.encode(signupDTO.getPassword());
    signupDTO.setPassword(encryptedPwd);

    MultipartFile file = signupDTO.getProfileFile();

    if (file == null || file.isEmpty()) {
      signupDTO.setProfileImg("avatar.png");
    }

    int result = userMapper.updateUserForSignup(signupDTO);

    log.info("signup:::::{}", result);

    return false;
  }

  @Override
  public int saveAutoLogin(String userId, String sessionId, LocalDateTime localDateTime) {

    return userMapper.updateUserAutoLogin(userId, sessionId, localDateTime);
  }


}
