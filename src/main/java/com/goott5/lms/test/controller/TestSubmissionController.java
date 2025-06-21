package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.test.answer.TestAnswerDTO;
import com.goott5.lms.test.domain.test.detail.result.dto.TestQuestionResultDTO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestQuestionResultVO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestRegisterResultVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionDTO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import com.goott5.lms.test.service.test.submission.TestSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "시험 제출", description = "시험 응시·제출 API")
@RequiredArgsConstructor
@Slf4j
public class TestSubmissionController {

  private final TestSubmissionService testSubmissionService;

  @GetMapping("/my/tests/{testId}/submission")
  public ResponseEntity<ApiResult<TestSubmissionVO>> getTestSubmission(
          @PathVariable(required = true) int testId, HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
            testSubmissionService.getTestSubmission(testId, session));
  }

  @PutMapping("/my/tests/{testId}/submission/abnormal")
  public ResponseEntity<ApiResult<String>> modifyTestSubmissionToInProgressIncrementAbnormalCount(
          @PathVariable int testId,
          @RequestBody TestAnswerDTO testAnswerDTO,
          HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
            testSubmissionService.modifyTestSubmissionToInProgressIncrementAbnormalCount(
                    testAnswerDTO,
                    session));
  }

  @PutMapping("/my/tests/{testId}/submission")
  public ResponseEntity<ApiResult<String>> modifySubmissionToCompleted(
          @PathVariable int testId,
          @RequestBody TestAnswerDTO testAnswerDTO,
          HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
            testSubmissionService.modifySubmissionToCompleted(testAnswerDTO, session));
  }

  @GetMapping("/my/tests/{testId}")
  public ResponseEntity<ApiResult<TestRegisterResultVO>> getTestResult(
          @PathVariable(required = true) int testId,
          HttpSession session
  ) {

    return ApiResult.respondOk(200, "SUCCESS",
            testSubmissionService.getTestResult(testId, session));
  }

  @GetMapping("/my/tests/{testId}/learner/{learnerId}")
  public ResponseEntity<ApiResult<TestRegisterResultVO>> getTestResultByLearnerId(
          @PathVariable(required = true) int testId,
          @PathVariable(required = true) int learnerId
  ) {

    System.out.println(">> getTestResult called: testId=" + testId + ", learnerId=" + learnerId);

    return ApiResult.respondOk(200, "SUCCESS",
            testSubmissionService.getTestResultByLearnerId(testId, learnerId));
  }


}
