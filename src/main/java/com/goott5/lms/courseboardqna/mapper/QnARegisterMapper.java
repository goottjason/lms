package com.goott5.lms.courseboardqna.mapper;

import com.goott5.lms.courseboardqna.domain.list.QnAListVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnARequestVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterVO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QnARegisterMapper {

  // 과정 ID(PK) Select
  @Select("SELECT id FROM course WHERE name = #{courseName}")
  Integer selectCourseId(String courseName);

  // 글 Insert
  @Insert("INSERT INTO course_qna (course_id, writer_id, title, content, is_secret)"
      + " VALUES (#{courseId}, #{writerId}, #{title}, #{content}, #{isSecret})")
  int insertQnAPost(QnARegisterVO qnaRegisterVO);

  // 게시글 Select
  List<QnAListVO> selectQnAPost(QnARequestVO qnaRequestVO);

  // 게시글 수 Select
  Integer selectQnAPostCount(QnARequestVO qnaRequestVO);
}
