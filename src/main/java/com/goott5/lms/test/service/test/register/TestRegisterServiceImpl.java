package com.goott5.lms.test.service.test.register;

import com.goott5.lms.test.domain.pagination.RequestVO;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.test.domain.test.list.TestListDTO;
import com.goott5.lms.test.domain.test.list.TestListVO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import com.goott5.lms.test.mapper.test.TestRegisterMapper;
import com.goott5.lms.test.mapper.test.TestMapperUtil;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TestRegisterServiceImpl implements TestRegisterService {

  private final TestRegisterMapper testRegisterMapper;

  @Override
  public ResponseVO<TestListDTO> getTestList(String courseName, int currentPageNo) {

    log.info("courseName : {}", courseName);

    RequestVO requestVO = RequestVO.builder()
            .currentPageNo(currentPageNo)
            .build();

    List<TestListVO> testListVOS = testRegisterMapper.selectTestLists(courseName, requestVO);
    List<TestListDTO> testListDTOS = new ArrayList<>();

    for (TestListVO testListVO : testListVOS) {

      testListDTOS.add(TestMapperUtil.toDTO(testListVO));
    }

    Integer totalTestCount = testRegisterMapper.selectTestListCount(courseName);
    requestVO.setTotalItemsCount(totalTestCount);

    return ResponseVO.<TestListDTO>allInfo()
            .requestVO(requestVO)
            .items(testListDTOS)
            .build();
  }

  @Transactional(rollbackFor = Exception.class, timeout = 30)
  @Override
  public void createTest(TestRegisterDTO testRegisterDTO, HttpSession session) {

    int instructorId = ((UserVO) session.getAttribute("loginUser")).getId();
    testRegisterDTO.setInstructorId(instructorId);
    TestRegisterVO testRegisterVO = TestMapperUtil.toVO(testRegisterDTO);

    // 강좌 ID(PK) 가져오기
    Integer courseId = testRegisterMapper.selectCourseId(testRegisterDTO.getCourseName());
    testRegisterVO.setCourseId(courseId);

    // 시험 등록
    testRegisterMapper.insertTest(testRegisterVO);
    log.info("testRegisterVO : {}", testRegisterVO);

    // 시험 문제 + 선택지 등록
    for (TestQuestionVO questionVO : testRegisterVO.getQuestions()) {

      questionVO.setTestId(testRegisterVO.getId());
      testRegisterMapper.insertQuestion(questionVO);

      if ("MULTIPLE".equals(questionVO.getQuestionType())) {

        for (TestOptionVO optionVO : questionVO.getOptions()) {

          optionVO.setQuestionId(questionVO.getId());
          testRegisterMapper.insertOption(optionVO);
        }
      }
    }

    // 교육생 ID(PK) 가져오기
    List<Integer> learnerIdList = testRegisterMapper.selectLearnerIdByCourseId(
            testRegisterDTO.getCourseName());
    log.info("learnerId : {}", learnerIdList);

    // 해당 시험에 등록된 모든 수강생의 시험 제출 상태를 초기화하여 저장
    for (Integer learnerId : learnerIdList) {

      TestSubmissionVO testSubmissionVO = TestSubmissionVO.builder()
              .testId(testRegisterVO.getId())
              .learnerId(learnerId)
              .submissionTime(testRegisterVO.getTestTime())
              .submissionStatus("NOT_STARTED")
              .build();
      testRegisterMapper.insertTestSubmissions(testSubmissionVO);
    }

  }

}
