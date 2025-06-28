package com.goott5.lms.user.interceptor;

import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptorForADMINISTRATORandINSTRUCTOR implements HandlerInterceptor {

  private final UserService userService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
          throws Exception {

    HttpSession session = request.getSession();
    UserVO userVO = (UserVO) session.getAttribute("loginUser");

    String type = userVO.getType();

    if (type.equals("LEARNER")) {
      response.sendRedirect("/user/invalidAccess");
      return false;
    } else {
      return true;
    }

  }
}
