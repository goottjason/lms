package com.goott5.lms.test.service.test.submission;

import com.goott5.lms.test.domain.test.answer.TestAnswerDTO;
import com.goott5.lms.test.domain.test.detail.result.dto.TestQuestionResultDTO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestQuestionResultVO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestRegisterResultVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionDTO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface TestSubmissionService {

  // 시험 제출 정보 가져오기
  TestSubmissionVO getTestSubmission(int testId, HttpSession session);

  TestSubmissionVO getTestSubmission2(int testId, int userId);

  // 시험 비정상적인 조작시 비정상 조작 카운트 증가
  String modifyTestSubmissionToInProgressIncrementAbnormalCount(TestAnswerDTO testAnswerDTO,
      HttpSession session);

  // 시험 최종 제출
  String modifySubmissionToCompleted(TestAnswerDTO testAnswerDTO, HttpSession session);

  // 시험 결과 가져오기
//  TestRegisterResultVO getTestResult(int testId, HttpSession session);
  TestRegisterResultVO getTestResult(int testId, int userId);

  TestRegisterResultVO getTestResultByLearnerId(int testId, int learnerId);

}
