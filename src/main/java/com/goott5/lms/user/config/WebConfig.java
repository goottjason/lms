package com.goott5.lms.user.config;

import com.goott5.lms.user.interceptor.AuthInterceptorForADMINISTRATOR;
import com.goott5.lms.user.interceptor.AuthInterceptorForADMINISTRATORandINSTRUCTOR;
import com.goott5.lms.user.interceptor.AuthInterceptorForINSTRUCTOR;
import com.goott5.lms.user.interceptor.AuthInterceptorForLEARNER;
import com.goott5.lms.user.interceptor.AuthInterceptorForLEARNERandINSTRUCTOR;
import com.goott5.lms.user.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginInterceptor loginInterceptor;
  private final AuthInterceptorForADMINISTRATOR authInterceptorForADMINISTRATOR;
  private final AuthInterceptorForINSTRUCTOR authInterceptorForINSTRUCTOR;
  private final AuthInterceptorForLEARNER authInterceptorForLEARNER;
  private final AuthInterceptorForADMINISTRATORandINSTRUCTOR authInterceptorForADMINISTRATORandINSTRUCTOR;
  private final AuthInterceptorForLEARNERandINSTRUCTOR authInterceptorForLEARNERandINSTRUCTOR;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 로그인(자동로그인 포함)여부 체크 인터셉터
    registry.addInterceptor(loginInterceptor).addPathPatterns("/**")
            .excludePathPatterns("/user/signup", "/user/login", "/user/needLogin", "/user/logout",
                    "/css/**", "/js/**",
                    "/images/**", "/.well-known/**",
                    "/fonts/**", "/img/**", "/favicon.ico", "/vendor/**", "/api/**", "/error/**");

    registry.addInterceptor(authInterceptorForADMINISTRATOR)
            .addPathPatterns("/courseRegister", "/courseManagement/courseModify",
                    "/learnerManagement/employment", "/cancelDateManagement",
                    "/operationsManagement/userRegister", "/communityInquiry/answerRegister",
                    "/communityInquiry/answerDelete");

    registry.addInterceptor(authInterceptorForINSTRUCTOR)
            .addPathPatterns("/homework/homeworkRegister", "/homework/homeworkModify",
                    "/homework/deleteHomework", "/homework/evalRegister",
                    "/homework/modifyEvalPost", "/homework/deleteEval");

    registry.addInterceptor(authInterceptorForLEARNER)
            .addPathPatterns("/participation/participationView", "/homework/submissionRegister",
                    "/homework/submissionModify", "/homework/submissionDelete");

    registry.addInterceptor(authInterceptorForADMINISTRATORandINSTRUCTOR)
            .addPathPatterns("/learnerManagement/learnerList", "/learnerManagement/learnerDetail",
                    "/learnerManagement/employment", "/courseBoardMaterials/materialsRegister",
                    "/traininglog/traningList", "/traininglog/traningDetail");

    registry.addInterceptor(authInterceptorForLEARNERandINSTRUCTOR)
            .addPathPatterns("/communityInquiry/inquiryRegister",
                    "/communityInquiry/inquiryModify", "/communityInquiry/inquiryDelete");

  }

}
