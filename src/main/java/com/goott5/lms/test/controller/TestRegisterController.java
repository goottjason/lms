package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResponse;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.test.domain.test.list.TestListDTO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.service.test.register.TestRegisterService;
import com.goott5.lms.user.domain.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "시험", description = "강좌 조회·등록 API")
@RequiredArgsConstructor
@Slf4j
public class TestRegisterController {

  private final TestRegisterService testRegisterService;

  @GetMapping("/tests")
  @Operation(summary = "시험", description = "시험 리스트를 조회하는 메서드입니다.", parameters = {
      @Parameter(in = ParameterIn.HEADER, name = "courseName", description = "특정 강좌만의 리스트를 조회하기 위한 조건", required = false, example = "자바 스프링 1회차"),
//      @Parameter(in = ParameterIn.HEADER, name= "currentPageNo", default)
  })
  public ResponseEntity<ApiResponse<ResponseVO<TestListDTO>>> getTestLst(
      @RequestParam(required = false) String courseName,
      @RequestParam(defaultValue = "1") int currentPageNo,
      HttpSession session) {
    log.info("courseName: {}", courseName);
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    ResponseVO<TestListDTO> responseVO = testRegisterService.getTestList(courseName,
        currentPageNo);

    return ApiResponse.respondOk(200, loginUser.getType(), responseVO);
  }

  @PostMapping("/tests")
  public <T> ResponseEntity<ApiResponse<T>> createTest(
      @Valid @RequestBody TestRegisterDTO testRegisterDTO,
      BindingResult bindingResult, HttpSession session) {

    if (bindingResult.hasErrors()) {

      Map<String, String> errorsMap = new HashMap<>();
      for (FieldError err : bindingResult.getFieldErrors()) {
        errorsMap.put(err.getField(), err.getDefaultMessage());
      }

      log.info("errors={}", errorsMap);

      return ApiResponse.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    testRegisterService.createTest(testRegisterDTO, session);

    return ApiResponse.respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

}
