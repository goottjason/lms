package com.goott5.lms.test.mapper.test;

import com.goott5.lms.test.domain.test.answer.QuestionScoreVO;
import com.goott5.lms.test.domain.test.answer.TestAnswerDTO;
import com.goott5.lms.test.domain.test.detail.result.vo.TestQuestionResultVO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionDTO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TestSubmissionMapper {

  // 특정 수강생의 시험 제출 정보 가져오기
  @Select(
          "SELECT id, submission_time, submission_status, retry_count, is_invalidated, submission_reg_date, score"
                  + " FROM test_submission WHERE test_id = #{testId} AND learner_id = #{learnerId}")
  TestSubmissionVO selectTestSubmission(@Param("testId") int testId,
          @Param("learnerId") int learnerId);

  // 특정 수강생의 시험 제출 정보 가져오기
  @Select(
      "SELECT id, submission_time, submission_status, retry_count, is_invalidated, submission_reg_date, score"
          + " FROM test_submission WHERE test_id = #{testId} AND learner_id = #{learnerId}")
  TestSubmissionVO selectTestSubmission2(@Param("testId") int testId,
      @Param("learnerId") int learnerId);

  // 답안 제출
  @Insert("INSERT INTO test_answer (question_id , submission_id , select_answer, is_correct)"
          + " VALUES (#{questionId}, #{submissionId}, #{selectAnswer}, #{isCorrect})")
  void insertTestAnswer(@Param("questionId") int id,
          @Param("submissionId") Integer testSubmissionId,
          @Param("selectAnswer") String selecteAnswer,
          @Param("isCorrect") boolean isCorrect);

  // 정답 문항들만 가져오기
  List<QuestionScoreVO> selectCorrectedQuestion(@Param("testId") int testId,
          @Param("testSubmissionId") Integer testSubmissionId);

  // 응시자의 제출 정보 Update
  void updateTestSubmission(@Param("testSubmissionVO") TestSubmissionVO testSubmissionVO);

  // 시험 무효자의 제출 응답 Delete
  @Delete("DELETE FROM test_answer WHERE submission_id = #{testSubmissionId}")
  void deleteTestAnswer(Integer testSubmissionId);

  // 응시자의 제출 답안 가져오기
  TestAnswerDTO selectSelectAnswers(Integer testSubmissionId);

  // 응시자의 제출 답안 Update (정답 여부 Update)
  @Update("UPDATE test_answer SET is_correct = #{isCorrect}"
          + " WHERE question_id = #{id} AND submission_id = #{testSubmissionId}")
  void updateTestAnswer(@Param("questionId") int questionId,
          @Param("testSubmissionId") Integer testSubmissionId, @Param("isCorrect") boolean isCorrect);

  // 시험 결과 Select
  List<TestQuestionResultVO> selectTestResult(@Param("testId") int testId,
          @Param("submissionId") int submissionId);

  // 시험 점수 Select
  @Select("SELECT score FROM test_submission WHERE id = #{submissionId}")
  int selectUserScore(int submissionId);

  // 자동 종료 처리
  void updateNoShowToZero(int testId);

  // 자동 종료 처리가 완료된 테스트는 true로 설정
  void markAutoGraded(@Param("testId") int testId);
}
