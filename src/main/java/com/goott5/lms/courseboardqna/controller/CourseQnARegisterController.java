package com.goott5.lms.courseboardqna.controller;

import com.goott5.lms.courseboardqna.domain.list.QnAListVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnARequestVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnAResponseVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.courseboardqna.service.register.QnARegisterService;
import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseQnARegisterController {

  private final QnARegisterService qnaRegisterService;

  @GetMapping("/qna")
  public ResponseEntity<ApiResult<QnAResponseVO<QnAListVO>>> getQnAList(
      @ModelAttribute QnARequestVO qnaRequestVO,
      HttpSession session
  ) {

    return ApiResult.respondOk(200, ((UserVO) session.getAttribute("loginUser")).getType(),
        qnaRegisterService.getQnAList(qnaRequestVO));
  }

  @PostMapping("/qna")
  public <T> ResponseEntity<ApiResult<T>> createQnA(
      @Valid @RequestBody QnARegisterDTO qnaRegisterDTO, BindingResult bindingResult,
      HttpSession session
  ) {

    // 유효성 검사
    if (bindingResult.hasErrors()) {
      Map<String, String> errorsMap = new HashMap<>();

      for (FieldError error : bindingResult.getFieldErrors()) {
        errorsMap.put(error.getField(), error.getDefaultMessage());
      }
      return ApiResult.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    qnaRegisterService.createQnA(qnaRegisterDTO, session);

    return ApiResult.<T>respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

}
