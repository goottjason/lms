package com.goott5.lms.user.config;

import com.goott5.lms.user.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginInterceptor loginInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 로그인여부 체크 인터셉터
    registry.addInterceptor(loginInterceptor).addPathPatterns("/**")
            .excludePathPatterns("/user/signup", "/user/login", "/user/logout", "/css/**", "/js/**",
                    "/images/**", "/.well-known/**",
                    "/fonts/**", "/img/**", "/favicon.ico", "/vendor/**", "/api/**", "/error/**");

  }


}
