package com.goott5.lms.test.service.test.detail;

import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.mapper.test.TestDetailMapper;
import com.goott5.lms.test.mapper.test.TestMapperUtil;
import com.goott5.lms.test.mapper.test.TestRegisterMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDetailServiceImpl implements TestDetailService {

  private final TestRegisterMapper testRegisterMapper;

  private final TestDetailMapper testDetailMapper;


  @Override
  public TestRegisterDTO getTestDetail(int testId) {
    TestRegisterVO testDetailVO = testDetailMapper.selectTestDetail(testId);

    return TestMapperUtil.toDTO(testDetailVO);
  }

  @Override
  public List<LearnerInfoVO> getTestLearners(int testId) {

    return testDetailMapper.selectLearnerInfo(testId);
  }

  @Override
  public void modifyTestDetail(TestRegisterDTO testDetailDTO, int testId) {

    // VO로 변환
    TestRegisterVO testDetailVO = TestMapperUtil.toVO(testDetailDTO);
    testDetailVO.setId(testId);
    log.info("testDetailVO:{}", testDetailVO);

    // 시험 기본 정보 수정
    testDetailMapper.updateTestInfo(testDetailVO);

    // 시험 문항 정보 수정
    // 기존 시험 문항 정보 가져오기
    TestRegisterVO testRegisterVO = testDetailMapper.selectTestDetail(testId);

    int currentQuestionSize = testRegisterVO.getQuestions().size();
    int newQuestionSize = testDetailVO.getQuestions().size();
    int questionCommon = Math.min(currentQuestionSize, newQuestionSize);

    for (int i = 0; i < questionCommon; i++) {

      TestQuestionVO dbQ = testRegisterVO.getQuestions().get(i);
      log.info("dbQ : {}", dbQ);
      TestQuestionVO inQ = testDetailVO.getQuestions().get(i);
      inQ.setTestId(testId);
      log.info("inQ : {}", inQ);

      // 기존 문제 유형과 같은 경우
      if (dbQ.getQuestionType().equals(inQ.getQuestionType())) {
        if ("SHORT".equals(inQ.getQuestionType())) {
          // "SHORT"인 경우
          testDetailMapper.updateSameTypeQuestion(inQ);
        } else if ("MULTIPLE".equals(inQ.getQuestionType())) {
          // "MULTIPLE"인 경우

          // 문항 Update
          testDetailMapper.updateSameTypeQuestion(inQ);

          int currentOptionSize = dbQ.getOptions().size();
          int newOptionSize = inQ.getOptions().size();
          int optionCommon = Math.min(currentOptionSize, newOptionSize);

          for (int j = 0; j < optionCommon; j++) {
            // 공통되는 문항 수 대로 Update

            inQ.getOptions().get(j).setQuestionId(dbQ.getId());
            log.info("inQ.getOptions().get(j) : {}", inQ.getOptions().get(j));
            testDetailMapper.updateSameMultipleTypeOption(inQ.getOptions().get(j));
          }

          if (currentOptionSize < newOptionSize) {
            for (int k = currentOptionSize; k < newOptionSize; k++) {
              // 추가되는 선택지가 더 많은 경우 Insert

              inQ.getOptions().get(k).setQuestionId(dbQ.getId());
              testRegisterMapper.insertOption(inQ.getOptions().get(k));
            }
          }

          if (currentOptionSize > newOptionSize) {
            for (int p = newOptionSize; p < currentOptionSize; p++) {
              // 추가되는 선택지가 더 적은 경우 Delete

              testDetailMapper.deleteMultipleOption(dbQ.getId(),
                  dbQ.getOptions().get(p).getOptionNo());
            }
          }

        }
      } else {
        // 기존 문제 유형과 다른 경우
        if ("SHORT".equals(inQ.getQuestionType())) {
          // 새 유형이 "SHORT"인 경우

          // 해당 문항의 기존 선택지 모두 삭제
          testDetailMapper.deleteMultipleOption(dbQ.getId(), null);
          testDetailMapper.updateDiffTypeQuestion(inQ);
        } else if ("MULTIPLE".equals(inQ.getQuestionType())) {
          // 새 유형이 "MULTIPLE"인 경우

          // correct_answer = "" Update

          testDetailMapper.updateDiffTypeQuestion(inQ);
          // 선택지 Insert
          for (TestOptionVO testOptionVO : inQ.getOptions()) {
            testOptionVO.setQuestionId(dbQ.getId());
            testRegisterMapper.insertOption(testOptionVO);
          }
        }
      }
    }
    if (currentQuestionSize > newQuestionSize) {
      // 기존 문항의 수가 더 많은 경우
      for (int t = questionCommon; t < currentQuestionSize; t++) {
        TestQuestionVO dbQ = testRegisterVO.getQuestions().get(t);

        if ("MULTIPLE".equals(dbQ.getQuestionType())) {
          testDetailMapper.deleteMultipleOption(dbQ.getId(), null);
        }

        testDetailMapper.deleteQuestion(dbQ);
      }
    }

    if (currentQuestionSize < newQuestionSize) {
      // 추가된 문항들이 더 많은 경우
      for (int r = questionCommon; r < newQuestionSize; r++) {
        TestQuestionVO inQ = testDetailVO.getQuestions().get(r);
        inQ.setTestId(testId);

        // 문항 Insert
        testRegisterMapper.insertQuestion(inQ);
        if ("MULTIPLE".equals(inQ.getQuestionType())) {

          for (TestOptionVO optionVO : inQ.getOptions()) {

            optionVO.setQuestionId(inQ.getId());
            testRegisterMapper.insertOption(optionVO);
          }
        }
      }
    }
  }

  @Override
  public void removeTestDetail(int testId) {
    testDetailMapper.deleteTest(testId);
  }

}
