package com.goott5.lms.communityInquiry.controller;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.service.InquiryService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/communityInquiry")
@RequiredArgsConstructor
@Slf4j
public class InquiryController {

  private final InquiryService inquiryService;

  @GetMapping("/inquiryList")
  public String inquiryList(HttpServletRequest request,
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam,
          Model model) {

    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();
    String userType = ((UserVO) request.getSession().getAttribute("loginUser")).getType();

    inquiryRequestParam.setUserId(userId);
    inquiryRequestParam.setUserType(userType);

    log.info("inquiryRequestParam: {}", inquiryRequestParam);

    InquiryListResponse inquiryListResponse = inquiryService.getInquiryList(inquiryRequestParam);

    log.info(inquiryListResponse.toString());

    model.addAttribute("inquiryListResponse", inquiryListResponse);

    return "/communityInquiry/inquiryList";

  }
}