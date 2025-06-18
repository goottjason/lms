package com.goott5.lms.user.service;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.service.SendEmailService;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.mapper.UserMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.mail.MessagingException;
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
  private final SendEmailService sendEmailService;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;

  @Override
  public UserVO findUserByLoginId(String loginId) {
    return userMapper.selectUserByLoginId(loginId);
  }

  @Override
  public UserVO findUserByEmail(String email) {
    return userMapper.selectUserByEmail(email);
  }

  @Override
  public UserVO findUserByMobile(String mobile) {
    return userMapper.selectUserByMobile(mobile);
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
  public int saveAutoLogin(int userId, String sessionId, LocalDateTime localDateTime) {

    return userMapper.updateUserAutoLogin(userId, sessionId, localDateTime);
  }

  @Override
  public String sendAuthCodeForSignup(String email) throws MessagingException {

    String title = "Goot5 LMS 회원가입을 위한 인증번호 메일입니다.";

    String authCode = UUID.randomUUID().toString();
    log.info("인증번호::::::::::::{}", authCode);

    String html = "<h1>회원가입을 환영합니다.</h1>";
    html += "<h2>인증번호를 입력하시고 회원가입을 완료하세요</h2>";
    html += "<h3>인증 코드 : " + authCode + "</h3>";

    sendEmailService.sendEmail(email, title, html);

    return authCode;
  }

  @Override
  public boolean signup(SignupDTO signupDTO) throws IOException {

    UserVO userVO = userMapper.selectUserByEmail(signupDTO.getEmail());
    signupDTO.setId(userVO.getId());

    String encryptedPwd = passwordEncoder.encode(signupDTO.getPassword());
    signupDTO.setPassword(encryptedPwd);

    MultipartFile file = signupDTO.getProfileFile();

    if (file == null || file.isEmpty()) {
      signupDTO.setProfileImg(
              "https://joon-s3upload.s3.ap-northeast-2.amazonaws.com/upload/user/avatar.png");
    } else {

      // 첨부 파일 서버에 저장 + 경로 저장
      // putObject 뒤에 경로 반환
      String insertPath = s3Uploader.uploadFile("upload/user", file.getInputStream(),
              file.getOriginalFilename());

      log.info("파일 서버 저장 성공");

      // 받은 파일 dto에 세팅
      // db 에서 해당 테이블의 게시글 id 다시 받아오기
      FileDTO fileDTO = FileDTO.builder()
              .originalName(file.getOriginalFilename())
              .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
              .path(insertPath)
              .size((int) file.getSize())
              .tableName("user")
              .tableId(userVO.getId())
              .build();

      // 첨부 파일 db에 저장
      int fileInsert = utilService.insertService(fileDTO);
      if (fileInsert == 1) {
        log.info("파일 db에 저장 성공:{}", fileDTO);
      }
      signupDTO.setProfileImg(insertPath);

    }

    int result = userMapper.updateUserForSignup(signupDTO);

    return false;
  }

}
