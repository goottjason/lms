package com.goott5.lms.user.controller;

import com.goott5.lms.user.domain.ApiResponse;
import com.goott5.lms.user.domain.ChangePwdDTO;
import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import javax.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@AllArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;
  private final BCryptPasswordEncoder passwordEncoder;

  @PostMapping("/login")
  public String login(LoginDTO loginDTO,
          HttpSession session,
          HttpServletRequest request,
          HttpServletResponse response,
          RedirectAttributes redirectAttributes) {

    UserVO userVO = userService.findUserByLoginId(loginDTO.getLoginId());
    if (userVO != null) {

      // 비밀번호 오입력 횟수 검사
      int wrongPasswordCount = userVO.getWrongPasswordCount();
      if (wrongPasswordCount == 5) {
        redirectAttributes.addFlashAttribute("msg", "비밀번호 입력 5회 실패하였습니다. 비밀번호를 변경해주세요.");
        return "redirect:/";
      }

      // 삭제회원 여부 검사
      if (userVO.getDeletedAt() != null) {
        redirectAttributes.addFlashAttribute("msg", "삭제된 ID입니다. 다른 ID로 로그인해주세요");
        return "redirect:/";
      }
    }

    // 로그인 처리
    UserVO loginUser = userService.login(loginDTO);

    if (loginUser != null) {
      session.setAttribute("loginUser", loginUser);

      // autoLogin 체크여부...
      if (loginDTO.isAutoLogin()) {
        saveAutoLogin(request, response);
      } else {
        userService.clearAutoLogin(loginDTO);
        Cookie cookie = new Cookie("autoLogin", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
      }

      String redirectUrl = "";
      switch (loginUser.getType()) {
        case "ADMINISTRATOR":
          redirectUrl = "home/administratorHome";
          break;
        case "INSTRUCTOR":
          redirectUrl = "home/instructorHome";
          break;
        case "LEARNER":
          redirectUrl = "home/learnerHome";
      }
      return "redirect:/" + redirectUrl;

    } else {
      redirectAttributes.addFlashAttribute("msg", "ID 또는 비밀번호가 잘못되었습니다.");
      return "redirect:/";
    }
  }


  private void saveAutoLogin(HttpServletRequest request, HttpServletResponse response) {

    String sessionId = request.getSession().getId();
    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();

    // 자동로그인 사용자에 대해 sessionId를 DB에 저장해놓는다.
    int result = userService.saveAutoLogin(userId, sessionId, LocalDateTime.now().plusDays(7));

    // sessionId를 사용자의 브라우저 쿠키에 저장시킨다.
    if (result == 1) {
      Cookie cookie = new Cookie("autoLogin", sessionId);
      cookie.setMaxAge(7 * 24 * 60 * 60);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }


  @GetMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response,
          HttpSession session) {

    if (session.getAttribute("loginUser") != null) {
      int userId = ((UserVO) session.getAttribute("loginUser")).getId();

      // 1. DB에서 해당 사용자의 sessionId 무효화
      int result = userService.saveAutoLogin(userId, null, null);

      // 2. 세션 무효화
      session.removeAttribute("loginUser");
      session.invalidate();
    }

    // 3. 쿠키 삭제
    Cookie cookie = new Cookie("autoLogin", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);

    return "redirect:/";

  }

  @GetMapping("/signup")
  public String signup() {

    return "user/signup";
  }


  @PostMapping("/idDuplicateCheck")
  public ResponseEntity<ApiResponse<String>> idDuplicateCheck(
          @RequestBody Map<String, String> data) {
    String loginId = data.get("loginId");
    UserVO userVO = userService.findUserByLoginId(loginId);
    if (userVO == null) {
      return ApiResponse.respondOk(200, "등록 가능ID", "등록가능한 ID입니다.");
    } else {
      return ApiResponse.respondFail(409, "등록 불가능ID", "사용 중인 ID입니다.", HttpStatus.CONFLICT);
    }
  }

  @PostMapping("/sendAuthCodeForSignup")
  public ResponseEntity<ApiResponse<String>> sendAuthCodeForSignup(
          @RequestBody Map<String, String> data, HttpSession session) {

    String email = data.get("email");
    String fullname = data.get("fullname");

    UserVO userVO = userService.findUserByEmail(email);

    if (userVO == null) {
      return ApiResponse.respondFail(409, "없는 이메일", "과정 신청 시 등록된 이메일 주소가 아닙니다.",
              HttpStatus.CONFLICT);
    } else if (!userVO.getFullName().equals(fullname)) {
      return ApiResponse.respondFail(409, "사용자 이름 오류", "이메일 주소가 등록된 사용자 이름과 다릅니다.",
              HttpStatus.CONFLICT);
    } else {
      String authCode = null;
      try {
        authCode = userService.sendAuthCodeForSignup(email);
        session.setAttribute("authCode", authCode);
        return ApiResponse.respondOk(200, "이메일 발송완료", "");
      } catch (MessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @PostMapping("/certificateAuthCode")
  public ResponseEntity<ApiResponse<String>> certificateAuthCode(
          @RequestBody Map<String, String> data, HttpSession session) {
    String certificateNo = data.get("certificateNo");

    if (session.getAttribute("authCode") != null) {
      String sesAuthCode = (String) session.getAttribute("authCode");

      if (certificateNo.equals(sesAuthCode)) {
        return ApiResponse.respondOk(200, "인증번호 확인 성공", "");
      } else {
        return ApiResponse.respondFail(409, "인증번호 확인 실패", "인증번호가 일치하지 않습니다.", HttpStatus.CONFLICT);
      }
    } else {
      return ApiResponse.respondFail(400, "세션정보 없음", "인증번호 발송여부를 확인해주세요.", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/removeCertificateNo")
  public ResponseEntity<ApiResponse<String>> removeCertificateNo(HttpSession session) {

    if (session.getAttribute("authCode") != null) {
      session.removeAttribute("authCode");
      return ApiResponse.respondOk(200, "인증번호 세션 삭제 성공", "");
    } else {
      return ApiResponse.respondFail(400, "세션정보 없음", "세션정보를 확인해주세요.", HttpStatus.BAD_REQUEST);
    }

  }

  @PostMapping("/mobileDuplicateCheck")
  public ResponseEntity<ApiResponse<String>> mobileDuplicateCheck(
          @RequestBody Map<String, String> data) {

    String mobile = data.get("mobile");
    UserVO userVO = userService.findUserByMobile(mobile);
    if (userVO == null) {
      return ApiResponse.respondOk(200, "등록 휴대폰", "등록가능한 휴대폰 번호입니다.");
    } else {
      return ApiResponse.respondFail(409, "등록 불가능 휴대폰", "등록이 불가한 휴대폰 번호입니다.", HttpStatus.CONFLICT);
    }
  }

  @PostMapping("/signup")
  public String signup(SignupDTO signupDTO, RedirectAttributes redirectAttributes) {

    log.info("signupDTO:::::::{}", signupDTO);

    try {
      userService.signup(signupDTO);
      redirectAttributes.addFlashAttribute("signupMsg", "회원가입에 성공하였습니다. 로그인해주세요.");
      return "redirect:/";
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/mypage")
  public String mypage() {

    return "user/mypage";
  }

  @PostMapping("/modifyProfileImg")
  public ResponseEntity<ApiResponse<String>> modifyProfileImg(HttpServletRequest request,
          @RequestParam("profileFile") MultipartFile profileFile) {

    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();

    try {
      String insertPath = userService.modifyProfileImg(profileFile, userId);
      ((UserVO) request.getSession().getAttribute("loginUser")).setProfileImg(insertPath);
      return ApiResponse.respondOk(200, "success", "프로필 이미지 변경 성공");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @PostMapping("/changePassword")
  public ResponseEntity<ApiResponse<String>> changePassword(HttpServletRequest request,
          @RequestBody Map<String, String> data) {

    String currentPassword = data.get("currentPassword");
    String newPassword = data.get("newPassword");

    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();
    String userPassword = ((UserVO) request.getSession().getAttribute("loginUser")).getPassword();

    if (!passwordEncoder.matches(currentPassword, userPassword)) {
      return ApiResponse.respondFail(409, "비밀번호 오류", "현재 비밀번호가 올바르지 않습니다.", HttpStatus.CONFLICT);
    } else {

      String password = userService.changePassword(userId, newPassword);
      ((UserVO) request.getSession().getAttribute("loginUser")).setPassword(password);

      return ApiResponse.respondOk(200, "성공", "비밀번호가 변경되었습니다.");
    }

  }


  @GetMapping("/changePwd")
  public String changePwd() {

    return "user/changePwd";
  }

  @PostMapping("/changePwdForSignup")
  public String changePwdForSignup(ChangePwdDTO changePwdDTO,
          RedirectAttributes redirectAttributes) {

    userService.changePwdForSignup(changePwdDTO);

    redirectAttributes.addFlashAttribute("signupMsg", "비밀번호가 변경되었습니다. 로그인해주세요.");
    return "redirect:/";
  }

  @GetMapping("/needLogin")
  public String needLogin() {
    return "user/needLogin";
  }

  @GetMapping("/invalidAccess")
  public String invalidAccess() {
    return "user/invalidAccess";
  }

}
