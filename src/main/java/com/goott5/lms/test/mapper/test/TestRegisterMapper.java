package com.goott5.lms.test.mapper.test;

import com.goott5.lms.test.domain.pagination.RequestVO;
import com.goott5.lms.test.domain.test.list.TestListVO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestRegisterMapper {

  // 시험 리스트 가져오기
  List<TestListVO> selectTestLists(@Param("courseName") String courseName,
          @Param("requestVO") RequestVO requestVO);

  // 시험 리스트 수 가져오기
  Integer selectTestListCount(String courseName);

  // 강좌 ID(PK) 가져오기
  @Select("SELECT id FROM course WHERE name = #{courseName}")
  Integer selectCourseId(String courseName);

  // 시험 등록
  void insertTest(TestRegisterVO testRegisterVO);

  // 시험 문제 + 선택지 등록
  void insertQuestion(TestQuestionVO testQuestionVO);

  void insertOption(TestOptionVO testOptionVO);

  // 교육생의 ID(PK) 가져오기
  List<Integer> selectLearnerIdByCourseId(String courseName);

  // 해당 시험에 등록된 모든 수강생의 시험 제출 상태를 초기화하여 저장
  void insertTestSubmissions(TestSubmissionVO testSubmissionVO);

}
