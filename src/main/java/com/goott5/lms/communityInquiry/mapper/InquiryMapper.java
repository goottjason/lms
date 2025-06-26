package com.goott5.lms.communityInquiry.mapper;

import com.goott5.lms.communityInquiry.domain.InquiryRequestDTO;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InquiryMapper {

  // 게시글 카운트
  int selectCountBoard(InquiryRequestParam inquiryRequestParam);

  List<InquiryVO> selectInquiryList(InquiryRequestParam inquiryRequestParam);

  @Select("select id, title, inquiry, writer, is_posted, is_answered, is_answered_checked, answer, answerer, created_at, updated_at, deleted_at, answered_at from community_inquiry where id = #{id}")
  InquiryVO selectInquiryDetail(@Param("id") int id);

  @Insert("insert into community_inquiry (title, inquiry, writer) values (#{title}, #{inquiry}, #{writer})")
  int insertInquiry(InquiryRequestDTO inquiryRequestDTO);

  @Update("update community_inquiry set title = #{title}, inquiry = #{inquiry}, updated_at = now() where id = #{id}")
  int updateInquiry(InquiryRequestDTO inquiryRequestDTO);

  @Update("update community_inquiry set is_posted = 0, deleted_at = now() where id = #{id}")
  int updateInquiryForDelete(@Param("id") int id);

  @Update("update community_inquiry set answer = #{answer}, answerer = #{answerer}, is_answered = 1, answered_at = now() where id = #{id}")
  int updateInquiryForAnswer(@Param("id") int id, @Param("answer") String answer,
          @Param("answerer") int answerer);

  @Update("update community_inquiry set answer = null, answerer = null, is_answered = 0, deleted_at = now() where id = #{id}")
  int updateInquiryForAnswerDelete(int id);

  @Update("update community_inquiry set is_answered_checked = 1 where id = #{id}")
  void updateInquiryForAnsweredChecked(@Param("id") int id);
}
