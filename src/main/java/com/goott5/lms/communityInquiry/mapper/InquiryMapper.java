package com.goott5.lms.communityInquiry.mapper;

import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InquiryMapper {

  // 전체 게시글 카운트
  @Select("select count(*) from community_inquiry")
  int selectCountBoard();

  // 조건 검색 게시글 카운트
  int selectCountBoardWithSearchCondition(InquiryRequestParam inquiryRequestParam);


  List<InquiryVO> selectInquiryList(InquiryRequestParam inquiryRequestParam);

}
