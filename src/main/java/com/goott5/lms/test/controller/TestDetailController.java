package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResponse;
import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.service.test.detail.TestDetailService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TestDetailController {

  private final TestDetailService testDetailService;

  @GetMapping("/tests/{testId}")
  public ResponseEntity<ApiResponse<TestRegisterDTO>> getTestDetail(
      @PathVariable("testId") int testId) {

    TestRegisterDTO testDetail = testDetailService.getTestDetail(testId);

    return ApiResponse.respondOk(200, "SUCCESS", testDetail);
  }

  @GetMapping("/tests/{testId}/learners")
  public ResponseEntity<ApiResponse<List<LearnerInfoVO>>> getTestLearners(
      @PathVariable("testId") int testId) {

    return ApiResponse.respondOk(200, "SUCCESS", testDetailService.getTestLearners(testId));
  }

  @PutMapping("/tests/{testId}")
  public <T> ResponseEntity<ApiResponse<T>> modifyTestDetail(@PathVariable("testId") int testId,
      @Valid @RequestBody TestRegisterDTO testDetailDTO, BindingResult bindingResult) {

    log.info("testDetailDTO: {}", testDetailDTO);

    if (bindingResult.hasErrors()) {
      Map<String, String> errorsMap = new HashMap<>();

      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errorsMap.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
      return ApiResponse.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    testDetailService.modifyTestDetail(testDetailDTO, testId);

    return ApiResponse.<T>respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

}
