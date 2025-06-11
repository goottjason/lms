package com.goott5.lms.user.controller;

import com.goott5.lms.user.domain.LoginDTO;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@AllArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @PostMapping("/login")
  public String login(LoginDTO loginDTO,
          HttpSession session,
          HttpServletRequest request,
          HttpServletResponse response,
          RedirectAttributes redirectAttributes) {

    UserVO userVO = userService.findUser(loginDTO.getLoginId());
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

      // autoLogin 체크한 유저...
      if (loginDTO.isAutoLogin()) {
        saveAutoLogin(request, response);
      }

      String redirectUrl = "";
      switch (loginUser.getType()) {
        case "ADMINISTRATOR":
          redirectUrl = "courseList";
          break;
        case "INSTRUCTOR":
          redirectUrl = "homeworkList";
          break;
        case "LEARNER":
          redirectUrl = "materialsList";
      }
      return "redirect:/" + redirectUrl;

    } else {
      redirectAttributes.addFlashAttribute("msg", "ID 또는 비밀번호가 잘못되었습니다.");
      return "redirect:/";
    }
  }


  private void saveAutoLogin(HttpServletRequest request, HttpServletResponse response) {

    String sessionId = request.getSession().getId();
    String userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();

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
  public String logout(HttpSession session) {

    if (session.getAttribute("loginUser") != null) {
      session.removeAttribute("loginUser");
      session.invalidate();
    }
    return "redirect:/";
  }


}
