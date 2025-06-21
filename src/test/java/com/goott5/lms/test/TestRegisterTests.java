package com.goott5.lms.test;

import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestOptionResultVO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestQuestionResultVO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.mapper.test.TestDetailMapper;
import com.goott5.lms.test.mapper.test.TestRegisterMapper;
import com.goott5.lms.test.mapper.test.TestSubmissionMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TestRegisterTests {

  @Autowired
  private TestRegisterMapper testRegisterMapper;

  @Autowired
  private TestDetailMapper testDetailMapper;

  @Autowired
  private TestSubmissionMapper testSubmissionMapper;

  @Test
  void getTestDetailTest() {

    int testId = 1021;

    TestRegisterVO testRegisterVO = testDetailMapper.selectTestDetail(testId);

    log.info("testRegisterVO: {}", testRegisterVO);
    for (TestQuestionVO testQuestionVO : testRegisterVO.getQuestions()) {
      log.info("testQuestionVO: {}", testQuestionVO);

      if (testQuestionVO.getQuestionType().equals("MULTIPLE")) {

        for (TestOptionVO testOptionVO : testQuestionVO.getOptions()) {

          log.info("testOptionVO: {}", testOptionVO);
        }
      }
    }
  }

  @Test
  void getLearnerInfo() {

    int testId = 8029;

    List<LearnerInfoVO> learnerInfoVOS = testDetailMapper.selectLearnerInfo(testId);
    for (LearnerInfoVO learnerInfoVO : learnerInfoVOS) {
      log.info("learnerInfoVO: {}", learnerInfoVO);
    }

  }

  @Test
  void getTestResult() {
    int testId = 8039;

    List<TestQuestionResultVO> testQuestionResultVOS = testSubmissionMapper.selectTestResult(
        testId, 49);

    for (TestQuestionResultVO testquestionResultVO : testQuestionResultVOS) {
      log.info("testQuestionResultVO: {}", testquestionResultVO);

      for (TestOptionResultVO testOptionResultVO : testquestionResultVO.getOptions()) {
        log.info("testOptionResultVO: {}", testOptionResultVO);
      }
    }

  }

}
