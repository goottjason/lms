package com.goott5.lms.courseboardqna.mapper;

import com.goott5.lms.courseboardqna.domain.detail.QnADetailVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface QnADetailMapper {

  // 상세 페이지 조회
  QnADetailVO selectQnADetail(int boardNo);

  // 글 수정
  void updateQnADetail(@Param("boardNo") int boardNo,
      @Param("qnaDTO") QnARegisterDTO qnaRegisterDTO);

  // 답변 등록
  @Update(
      "UPDATE course_qna SET comment = #{comment}, is_answer = true, comment_created_at = CURRENT_TIMESTAMP"
          + " WHERE id = #{boardNo}")
  void updateComment(@Param("comment") String comment, @Param("boardNo") int boardNo);

  // 답변 수정
  @Update("UPDATE course_qna SET comment = #{comment}, comment_updated_at = CURRENT_TIMESTAMP"
      + " WHERE id = #{boardNo}")
  void updateComment2(@Param("comment") String comment, @Param("boardNo") int boardNo);

  // 글 삭제
  @Update("UPDATE course_qna SET deleted_at = CURRENT_TIMESTAMP WHERE id = #{boardNo}")
  void deleteQnADetail(@Param("boardNo") int boardNo);

  // 답변 삭제
  @Update("UPDATE course_qna SET comment = null, is_answer = false WHERE id = #{boardNo}")
  void deleteQnAComment(@Param("boardNo") int boardNo);

}
