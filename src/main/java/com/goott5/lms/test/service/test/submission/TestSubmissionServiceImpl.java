package com.goott5.lms.test.service.test.submission;

import com.goott5.lms.test.domain.test.answer.QuestionScoreVO;
import com.goott5.lms.test.domain.test.answer.TestAnswerDTO;
import com.goott5.lms.test.domain.test.detail.result.dto.TestQuestionResultDTO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestQuestionResultVO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestRegisterResultVO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionDTO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import com.goott5.lms.test.mapper.test.TestDetailMapper;
import com.goott5.lms.test.mapper.test.TestSubmissionMapper;
import com.goott5.lms.test.service.test.detail.TestDetailService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import javax.print.DocFlavor.STRING;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestSubmissionServiceImpl implements TestSubmissionService {

  private final TestSubmissionMapper testSubmissionMapper;

  private final TestDetailMapper testDetailMapper;

  @Override
  public TestSubmissionVO getTestSubmission(int testId, HttpSession session) {

    int learnerId = ((UserVO) session.getAttribute("loginUser")).getId();

    return testSubmissionMapper.selectTestSubmission(testId, learnerId);
  }

  @Override
  public TestSubmissionVO getTestSubmission2(int testId, int userId) {

    return testSubmissionMapper.selectTestSubmission2(testId, userId);
  }


  @Override
  public String modifyTestSubmissionToInProgressIncrementAbnormalCount(TestAnswerDTO testAnswerDTO,
      HttpSession session) {

    TestSubmissionVO testSubmissionVO = testSubmissionMapper.selectTestSubmission(
        testAnswerDTO.getTestId(),
        ((UserVO) session.getAttribute("loginUser")).getId());

    // 응시자의 submission_status를 확인 후
    // IN_PROGRESS로 Update

    if (testSubmissionVO.getRetryCount() + 1 == 2) {
      // is_invalidated를 true로 변경

      TestSubmissionVO abnormalCompleted = TestSubmissionVO.builder()
          .id(testSubmissionVO.getId())
          .submissionTime(testAnswerDTO.getSubmissionTime())
          .submissionStatus("COMPLETED")
          .isInvalidated(true)
          .submissionRegDate(LocalDateTime.now())
          .score(0)
          .build();

      testSubmissionMapper.updateTestSubmission(abnormalCompleted);
      testSubmissionMapper.deleteTestAnswer(testSubmissionVO.getId());

      return "INVALIDATED";
    } else {
      // retry_count + 1
      // submissionTime
      // submissionStatus => "IN_PROGRESS"

      TestSubmissionVO abnormalInProgress = TestSubmissionVO.builder()
          .id(testSubmissionVO.getId())
          .submissionTime(testAnswerDTO.getSubmissionTime())
          .submissionStatus("IN_PROGRESS")
          .retryCount(testSubmissionVO.getRetryCount() + 1)
          .build();

      testSubmissionMapper.updateTestSubmission(abnormalInProgress);

      TestRegisterVO testRegisterVO = testDetailMapper.selectTestDetail(testAnswerDTO.getTestId());

      for (int i = 0; i < testAnswerDTO.getQuestionNums().size(); i++) {
        TestQuestionVO testQuestionVO = testRegisterVO.getQuestions().get(i);

        testSubmissionMapper.insertTestAnswer(testQuestionVO.getId(), testSubmissionVO.getId(),
            testAnswerDTO.getSelectAnswers().get(i), false);
      }

      return "COUNT1";
    }
  }


  @Override
  public String modifySubmissionToCompleted(TestAnswerDTO testAnswerDTO, HttpSession session) {

    // 제출 ID(PK)
    TestSubmissionVO testSubmissionVO = testSubmissionMapper.selectTestSubmission(
        testAnswerDTO.getTestId(),
        ((UserVO) session.getAttribute("loginUser")).getId());

    if ("IN_PROGRESS".equals(testSubmissionVO.getSubmissionStatus())) {

      testSubmissionMapper.deleteTestAnswer(testSubmissionVO.getId());
    }

    int userScore = 0;

    // 정상적인 시험 종료

    for (int i = 0; i < testAnswerDTO.getQuestionNums().size(); i++) {
      // 답안 가져오기
      TestRegisterVO testRegisterVO = testDetailMapper.selectTestDetail(testAnswerDTO.getTestId());

      // 채점
      TestQuestionVO answerKeyQuestion = testRegisterVO.getQuestions().get(i);
      String questionType = answerKeyQuestion.getQuestionType();

      if ("MULTIPLE".equals(questionType)) {
        // 객관식 문항의 경우
        String selectedMultipleAnswer = testAnswerDTO.getSelectAnswers().get(i);
        int correctMultipleAnswerNo = getCorrectMultipleAnswerNo(answerKeyQuestion.getOptions());

        boolean isCorrect = Integer.parseInt(selectedMultipleAnswer) == correctMultipleAnswerNo;

        testSubmissionMapper.insertTestAnswer(answerKeyQuestion.getId(),
            testSubmissionVO.getId(),
            selectedMultipleAnswer, isCorrect);


      } else if ("SHORT".equals(questionType)) {
        // 주관식 문항의 경우
        String selectedShortAnswer = testAnswerDTO.getSelectAnswers().get(i);
        String correctShortAnswer = answerKeyQuestion.getCorrectAnswer();

        boolean isCorrect = selectedShortAnswer.equals(correctShortAnswer);

        testSubmissionMapper.insertTestAnswer(answerKeyQuestion.getId(), testSubmissionVO.getId(),
            selectedShortAnswer, isCorrect);

      }
    }

    // 시험 점수 정산
    List<QuestionScoreVO> questionScoreVOS = testSubmissionMapper.selectCorrectedQuestion(
        testAnswerDTO.getTestId(),
        testSubmissionVO.getId());
    for (QuestionScoreVO questionScoreVO : questionScoreVOS) {
      userScore += questionScoreVO.getScore();
    }

    TestSubmissionVO normalCompleted = TestSubmissionVO.builder()
        .id(testSubmissionVO.getId())
        .submissionTime(testAnswerDTO.getSubmissionTime())
        .submissionStatus("COMPLETED")
        .submissionRegDate(LocalDateTime.now())
        .score(userScore)
        .build();
    // 시험 제출 정보 수정
    testSubmissionMapper.updateTestSubmission(normalCompleted);

    return "COMPLETED";
  }

  @Override
  public TestRegisterResultVO getTestResult(int testId, HttpSession session) {

    TestSubmissionVO testSubmissionVO = testSubmissionMapper.selectTestSubmission(testId,
        ((UserVO) session.getAttribute("loginUser")).getId());

    DecimalFormat df = new DecimalFormat("#0.00");
    double submissionTime = testSubmissionVO.getSubmissionTime() / 60.0;

    return TestRegisterResultVO.builder()
        .submissionTime(df.format(submissionTime))
        .userScore(testSubmissionMapper.selectUserScore(testSubmissionVO.getId()))
        .questions(testSubmissionMapper.selectTestResult(testId, testSubmissionVO.getId()))
        .build();
  }

  @Override
  public TestRegisterResultVO getTestResultByLearnerId(int testId, int learnerId) {

    TestSubmissionVO testSubmissionVO = testSubmissionMapper.selectTestSubmission(testId,
        learnerId);

    return TestRegisterResultVO.builder()
        .userScore(testSubmissionMapper.selectUserScore(testSubmissionVO.getId()))
        .questions(testSubmissionMapper.selectTestResult(testId, testSubmissionVO.getId()))
        .build();
  }

  private int getCorrectMultipleAnswerNo(List<TestOptionVO> options) {
    int answerNo = 0;

    for (TestOptionVO optionVO : options) {

      if (!optionVO.getIsCorrect()) {
        continue;
      }

      answerNo = optionVO.getOptionNo();
      break;
    }

    return answerNo;
  }
}