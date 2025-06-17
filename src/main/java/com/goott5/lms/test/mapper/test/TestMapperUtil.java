package com.goott5.lms.test.mapper.test;

import com.goott5.lms.test.domain.test.list.TestListDTO;
import com.goott5.lms.test.domain.test.list.TestListVO;
import com.goott5.lms.test.domain.test.list.TestStatus;
import com.goott5.lms.test.domain.test.register.dto.TestOptionDTO;
import com.goott5.lms.test.domain.test.register.dto.TestQuestionDTO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestMapperUtil {

  private static final DateTimeFormatter FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static TestListDTO toDTO(TestListVO vo) {
    return TestListDTO.builder()
            .id(vo.getId())
            .title(vo.getTitle())
            .courseName(vo.getCourseName())
            .startDate(vo.getStartDate())
            .endDate(vo.getEndDate())
            .testStatus(
                    TestStatus.determineStatus(vo.getStartDate(),
                            vo.getEndDate()))
            .testTime(vo.getTestTime() / 60)
            .completedCount(vo.getCompletedCount())
            .numberOfLearner(vo.getNumberOfLearner())
            .build();
  }

  public static TestRegisterVO toVO(TestRegisterDTO dto) {
    return TestRegisterVO.builder()
            .instructorId(dto.getInstructorId())
            .title(dto.getTestTitle())
            .startDate(LocalDateTime.parse(dto.getStartDate(), FORMATTER))
            .endDate(LocalDateTime.parse(dto.getEndDate(), FORMATTER))
            .testTime(dto.getTestTime() * 60)
            .totalScore(dto.getTotalScore())
            .questions(
                    dto.getQuestions().stream()
                            .map(TestMapperUtil::toVO)
                            .toList()
            )
            .build();

  }

  public static TestQuestionVO toVO(TestQuestionDTO dto) {
    return TestQuestionVO.builder()
            .title(dto.getQuestionTitle())
            .questionType(dto.getQuestionType())
            .questionNo(dto.getQuestionNo())
            .score(dto.getQuestionScore())
            .correctAnswer(dto.getQuestionAnswer())
            .options(
                    dto.getOptions().stream()
                            .map(TestMapperUtil::toVO)
                            .toList()
            )
            .build();
  }

  public static TestOptionVO toVO(TestOptionDTO dto) {
    return TestOptionVO.builder()
            .optionNo(dto.getOptionNo())
            .content(dto.getOptionContent())
            .isCorrect(dto.getIsCorrect())
            .build();
  }

  public static TestRegisterDTO toDTO(TestRegisterVO vo) {
    return TestRegisterDTO.builder()
            .id(vo.getId())
            .instructorId(vo.getInstructorId())
            .testTitle(vo.getTitle())
            .startDate(FORMATTER.format(vo.getStartDate()))
            .endDate(FORMATTER.format(vo.getEndDate()))
            .testTime(vo.getTestTime() / 60)
            .totalScore(vo.getTotalScore())
            .questions(
                    vo.getQuestions().stream()
                            .map(TestMapperUtil::toDTO)
                            .toList()
            )
            .build();
  }

  public static TestQuestionDTO toDTO(TestQuestionVO vo) {
    return TestQuestionDTO.builder()
            .id(vo.getId())
            .testId(vo.getTestId())
            .questionNo(vo.getQuestionNo())
            .questionTitle(vo.getTitle())
            .questionType(vo.getQuestionType())
            .questionScore(vo.getScore())
            .questionAnswer(vo.getCorrectAnswer())
            .options(
                    vo.getOptions().stream()
                            .map(TestMapperUtil::toDTO)
                            .toList()
            )
            .build();
  }

  public static TestOptionDTO toDTO(TestOptionVO vo) {
    return TestOptionDTO.builder()
            .id(vo.getId())
            .questionId(vo.getQuestionId())
            .optionNo(vo.getOptionNo())
            .optionContent(vo.getContent())
            .isCorrect(vo.getIsCorrect())
            .build();
  }


}
