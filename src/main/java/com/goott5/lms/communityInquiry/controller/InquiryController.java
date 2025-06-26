package com.goott5.lms.communityInquiry.controller;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestDTO;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import com.goott5.lms.communityInquiry.service.InquiryService;
import com.goott5.lms.user.domain.ApiResponse;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    InquiryListResponse inquiryListResponse = inquiryService.getInquiryList(inquiryRequestParam);

    model.addAttribute("inquiryListResponse", inquiryListResponse);

    return "communityInquiry/inquiryList";

  }

  @GetMapping("/inquiryDetail")
  public String inquiryDetail(
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam,
          HttpServletRequest request, Model model,
          RedirectAttributes redirectAttributes) {

    //inquiry Id 유효성 확인
    if (inquiryRequestParam.getId() == -1) {
      redirectAttributes.addFlashAttribute("message", "유효하지 않은 접근입니다.");
      return "redirect:/inquiryList";
    }

    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();
    inquiryRequestParam.setUserId(userId);

    InquiryVO inquiryVO = inquiryService.getInquiryDetail(inquiryRequestParam);

    if (inquiryVO == null) {
      redirectAttributes.addFlashAttribute("message", "유효하지 않은 접근입니다.");
      return "redirect:/inquiryList";
    } else {
      log.info(inquiryVO.toString());
      model.addAttribute("inquiryDetail", inquiryVO);
      return "communityInquiry/inquiryDetail";
    }
  }

  @GetMapping("/inquiryRegister")
  public String inquiryRegisterView(
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam) {
    return "communityInquiry/inquiryRegister";
  }

  @PostMapping("/inquiryRegister")
  public String inquiryRegister(HttpServletRequest request,
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam,
          @Valid InquiryRequestDTO inquiryRequestDTO, BindingResult bindingResult,
          RedirectAttributes redirectAttributes) {

    int userId = ((UserVO) request.getSession().getAttribute("loginUser")).getId();

    inquiryRequestDTO.setWriter(userId);

    int result = inquiryService.saveInquiry(inquiryRequestDTO);

    if (result != 1) {
      redirectAttributes.addFlashAttribute("message", "업로드에 실패했습니다. 다시 한번 시도해주세요.");
      return "redirect:/communityInquiry/inquiryRegister";
    } else {
      return "redirect:/communityInquiry/inquiryList";
    }
  }

  @GetMapping("/inquiryModify")
  public String inquiryModifyView(
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam,
          Model model) {
    InquiryVO inquiryVO = inquiryService.getInquiryDetail(inquiryRequestParam);

    model.addAttribute("inquiryDetail", inquiryVO);
    return "communityInquiry/inquiryModify";
  }

  @PostMapping("/inquiryModify")
  public String inquiryModify(HttpServletRequest request,
          @ModelAttribute("inquiryRequestParam") InquiryRequestParam inquiryRequestParam,
          @Valid InquiryRequestDTO inquiryRequestDTO, BindingResult bindingResult,
          RedirectAttributes redirectAttributes) {

    int id = inquiryRequestParam.getId();
    inquiryRequestDTO.setId(id);

    int result = inquiryService.updateInquiry(inquiryRequestDTO);

    if (result != 1) {
      redirectAttributes.addFlashAttribute("message", "수정에 실패했습니다. 다시 한번 시도해주세요.");
      return "redirect:/communityInquiry/inquiryModify";
    } else {
      return "redirect:/communityInquiry/inquiryDetail?" + inquiryRequestParam.getQueryString()
              + "&id=" + id;
    }
  }

  @GetMapping("/inquiryDelete")
  public String inquiryDelete(@RequestParam int id) {

    inquiryService.deleteInquiry(id);

    return "redirect:/communityInquiry/inquiryList";
  }

  @PostMapping("/answerRegister")
  public ResponseEntity<ApiResponse<String>> answerRegister(
          HttpServletRequest request,
          @RequestBody Map<String, String> data) {

    int answerer = ((UserVO) request.getSession().getAttribute("loginUser")).getId();
    int id = Integer.parseInt(data.get("id"));
    String answer = data.get("answer");

    inquiryService.updateInquiryForAnswer(id, answer, answerer);

    return ApiResponse.respondOk(200, "성공", "답변등록에 성공했습니다.");
  }

  @PostMapping("/answerDelete")
  public ResponseEntity<ApiResponse<String>> answerDelete(
          @RequestBody Map<String, String> data) {

    int id = Integer.parseInt(data.get("id"));

    inquiryService.updateInquiryForAnswerDelete(id);

    return ApiResponse.respondOk(200, "성공", "답변삭제에 성공했습니다.");
  }

}