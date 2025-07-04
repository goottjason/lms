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
            .excludePathPatterns("/user/login", "/user/logout", "/user/signup",
                    "/user/idDuplicateCheck", "/user/sendAuthCodeForSignup",
                    "/user/certificateAuthCode", "/user/removeCertificateNo",
                    "/user/mobileDuplicateCheck", "/user/changePwd", "/user/changePwdForSignup",
                    "/user/needLogin", "/user/invalidAccess", "/user/findId",
                    "/.well-known/**",
                    "/swagger-ui/**",
                    "/css/**", "/js/**",
                    "/images/**",
                    "/fonts/**", "/img/**", "/favicon.ico", "/vendor/**", "/api/**", "/error/**");

    registry.addInterceptor(authInterceptorForADMINISTRATOR)
            .addPathPatterns("home/administratorHome", "/courseRegister",
                    "/courseManagement/courseModify",
                    "/cancelDateManagement",
                    "/operationsManagement/userRegister", "/communityInquiry/answerRegister",
                    "/communityInquiry/answerDelete", "/admin/reports",
                    "/communityNotice/noticeRegister", "/communityNotice/noticeModify");

    registry.addInterceptor(authInterceptorForINSTRUCTOR)
            .addPathPatterns("home/instructorHome", "/homework/homeworkRegister",
                    "/homework/homeworkModify",
                    "/homework/deleteHomework", "/homework/evalRegister",
                    "/homework/modifyEvalPost", "/homework/deleteEval",
                    "/training/trainingRegister", "/training/trainingModify",
                    "/training/trainingDelete", "/test/register", "/vacation/vacationApproval");

    registry.addInterceptor(authInterceptorForLEARNER)
            .addPathPatterns("home/learnerHome", "/participation/participationView",
                    "/homework/submissionRegister",
                    "/homework/submissionModify", "/homework/submissionDelete",
                    "/test/testSubmission",
                    "/courseBoardQnA/register", "/courseBoardQnA/modify/**");

    registry.addInterceptor(authInterceptorForADMINISTRATORandINSTRUCTOR)
            .addPathPatterns("/learnerManagement/learnerList", "/learnerManagement/learnerDetail",
                    "/learnerManagement/employment", "/courseBoardMaterials/materialsRegister",
                    "/courseBoardMaterials/materialsModify",
                    "/training/trainingList", "/training/trainingDetail",
                    "/learnerManagement/employment", "/learnerManagement/sendEmail");

    registry.addInterceptor(authInterceptorForLEARNERandINSTRUCTOR)
            .addPathPatterns("/communityInquiry/inquiryRegister",
                    "/communityInquiry/inquiryModify", "/communityInquiry/inquiryDelete");

  }

}
