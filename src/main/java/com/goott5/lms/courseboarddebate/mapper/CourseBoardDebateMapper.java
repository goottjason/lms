package com.goott5.lms.courseboarddebate.mapper;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateCommentDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDetailInfo;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateLike;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateReport;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseBoardDebateMapper {

  int insertCourseBoardDebate(@Param("dto") CourseBoardDebateDTO courseBoardDebateDTO);

  List<CourseBoardDebateVO> selectCourseBoardDebateList(CourseBoardDebatePagingRequestDTO requestDTO);

  int selectCourseBoardDebateTotalCount(CourseBoardDebatePagingRequestDTO requestDTO);

  CourseBoardDebateDetailInfo selectCourseBoardDebateDetail(@Param("id") int id);

  int updateCourseBoardDebateReadCount(@Param("id") int id);

  int updateCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO);

  int softDeleteCourseBoardDebateById(@Param("id") int id);

  // 댓글
  List<CourseBoardDebateCommentDTO> findCourseBoardDebateCommentsByForumId(@Param("forumId") int forumId);
  int insertCourseBoardDebateComment(@Param("comment") CourseBoardDebateCommentDTO comment);
  CourseBoardDebateCommentDTO findCommentById(int commentId);
  int updateComment(CourseBoardDebateCommentDTO comment);
  int softDeleteComment(int commentId);

  // 좋아요
  int countCourseBoardDebateLikesByForumId(@Param("forumId") int forumId);
  CourseBoardDebateLike findCourseBoardDebateLikeByUserAndForum(@Param("forumId") int forumId, @Param("userId") int userId);
  int insertCourseBoardDebateLike(@Param("like") CourseBoardDebateLike like);
  int deleteCourseBoardDebateLike(@Param("like") CourseBoardDebateLike like);
  int updateCourseBoardDebateLikeCount(@Param("forumId") int forumId);

  // 신고
  int insertCourseBoardDebateReport(@Param("report") CourseBoardDebateReport report);
}
