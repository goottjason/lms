package com.goott5.lms.test.mapper.test;

import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.register.vo.TestOptionVO;
import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TestDetailMapper {

  // 특정 시험 상세 정보 가져오기
  TestRegisterVO selectTestDetail(@Param("testId") int testId);

  // 수강생 정보 가져오기
  List<LearnerInfoVO> selectLearnerInfo(@Param("testId") int testId);

  // 시험 기본 정보 수정
  @Update("UPDATE test"
      + " SET title = #{title}, start_date = #{startDate}, end_date = #{endDate}, test_time = #{testTime}, total_score = ${totalScore}"
      + " WHERE id = #{id}")
  void updateTestInfo(TestRegisterVO testDetailVO);

  // 동일한 유형 : 문항 Update
  void updateSameTypeQuestion(@Param("inQ") TestQuestionVO inQ);

  void updateDiffTypeQuestion(@Param("inQ") TestQuestionVO inQ);

  // 동일한 유형 | 객관식 => 객관식 : 선택지 Update
  void updateSameMultipleTypeOption(@Param("inO") TestOptionVO testOptionVO);

  // 문항 Delete
  void deleteQuestion(@Param("dbQ") TestQuestionVO dbQ);

  // 선택지 Delete
  void deleteMultipleOption(@Param("id") int id, @Param("optionNo") Integer optionNo);

}
