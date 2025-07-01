package com.goott5.lms.courseboarddebate.service;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateCommentDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDetailInfo;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePageDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateReport;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;

public interface CourseBoardDebateService {

  int insertCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO);

  CourseBoardDebatePagingResponseDTO<CourseBoardDebatePageDTO> getCourseBoardDebateList(
      CourseBoardDebatePagingRequestDTO requestDTO, HttpSession session);

  CourseBoardDebateDetailInfo getCourseBoardDebateDetail(int id, UserVO loginUser);

  boolean updateCourseBoardDebateReadCount(ReadCountLog readCountLog);

  int updateCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO);

  void deleteCourseBoardDebate(int debateId);

  void addCourseBoardDebateComment(CourseBoardDebateCommentDTO comment);

  int toggleCourseBoardDebateLike(int forumId, int userId);

  void addCourseBoardDebateReport(CourseBoardDebateReport report);

  void updateComment(int commentId, String content, UserVO loginUser);

  void deleteComment(int commentId, UserVO loginUser);

  void updateHotPostStatus(int forumId, int finalLikeCount, int finalCommentCount);
}
