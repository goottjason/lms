package com.goott5.lms.user.interceptor;

import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

  private final UserService userService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
          throws Exception {

    String requestURI = request.getRequestURI();

    log.info("requestURI: {}", requestURI);

    HttpSession session = request.getSession();

    Cookie autoLoginCookie = WebUtils.getCookie(request, "autoLogin");
    if (autoLoginCookie != null) {
      String sessionId = autoLoginCookie.getValue();

      // DB에서 자동로그인 체크한 유저를 확인하고, 자동로그인 시켜야 한다.
      UserVO autoLoginUser = userService.checkAutoLogin(sessionId);

      if (autoLoginUser != null) {
        session.setAttribute("loginUser", autoLoginUser);

        if (requestURI.equals("/")) {

          String redirectUrl = "";
          switch (autoLoginUser.getType()) {
            case "ADMINISTRATOR":
              redirectUrl = "/courseManagement/courseList";
              break;
            case "INSTRUCTOR":
              redirectUrl = "/homework/homeworkList";
              break;
            case "LEARNER":
              redirectUrl = "/participation/participationView";
          }
          response.sendRedirect(redirectUrl);
          return false;
        } else {
          return true;

        }
      } else {
        Cookie cookie = new Cookie("autoLogin", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.sendRedirect("/");
        return true;
      }
    } else {
      if (session == null || session.getAttribute("loginUser") == null) {
        if (requestURI.equals("/")) {
          return true;
        } else {
          response.sendRedirect("/");
          return false;
        }

      } else {
        if (requestURI.equals("/")) {

          String redirectUrl = "";
          switch (((UserVO) request.getSession().getAttribute("loginUser")).getType()) {
            case "ADMINISTRATOR":
              redirectUrl = "/courseManagement/courseList";
              break;
            case "INSTRUCTOR":
              redirectUrl = "/homework/homeworkList";
              break;
            case "LEARNER":
              redirectUrl = "/participation/participationView";
          }
          response.sendRedirect(redirectUrl);
          return false;
        } else {
          return true;
        }
      }
    }

  }

}
