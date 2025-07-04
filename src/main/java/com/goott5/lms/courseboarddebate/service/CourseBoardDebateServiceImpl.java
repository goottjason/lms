package com.goott5.lms.courseboarddebate.service;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateCommentDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDetailInfo;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateLike;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePageDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateReport;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateVO;
import com.goott5.lms.courseboarddebate.mapper.CourseBoardDebateMapper;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.notification.domain.NotificationSaveDTO;
import com.goott5.lms.notification.mapper.NotificationMapper;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseBoardDebateServiceImpl implements CourseBoardDebateService {

  private final CourseBoardDebateMapper courseBoardDebateMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final CourseManagementService courseManagementService;
  private final NotificationMapper notificationMapper;
  private final SimpMessagingTemplate messagingTemplate;


  @Override
  public CourseBoardDebatePagingResponseDTO<CourseBoardDebatePageDTO> getCourseBoardDebateList(
      CourseBoardDebatePagingRequestDTO requestDTO, HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    // 관리자가 아닐 경우, 접근 가능한 과정 ID 목록을 설정 (기존 로직과 동일)
    if (loginUser != null && !"ADMINISTRATOR".equals(loginUser.getType())) {
      CommonReqDTO commonReqDTO = CommonReqDTO.builder()
          .loginUserId(loginUser.getId())
          .loginUserType(loginUser.getType())
          .build();
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = PageCourseReqDTO.<CourseReqDTO>builder()
          .pageNo(1)
          .pageSize(1000)
          .build();
      PageCourseRespDTO<CourseRespDTO> courses = courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

      if (courses != null && !courses.getRespDTOS().isEmpty()) {
        List<Integer> accessibleCourseIds = courses.getRespDTOS().stream()
            .map(CourseRespDTO::getId)
            .collect(Collectors.toList());
        requestDTO.setAccessibleCourseIds(accessibleCourseIds);
      } else {
        return CourseBoardDebatePagingResponseDTO.<CourseBoardDebatePageDTO>allInfo()
            .courseBoardDebatePagingRequestDTO(requestDTO)
            .dtoList(new ArrayList<>())
            .total(0)
            .build();
      }
    }

    // 하나의 쿼리로 정렬된 전체 목록(인기글+일반글)을 가져옴
    List<CourseBoardDebateVO> posts = courseBoardDebateMapper.selectPosts(requestDTO);
    int totalCount = courseBoardDebateMapper.selectPostsTotalCount(requestDTO);

    // VO 리스트를 화면에 보여줄 PageDTO 리스트로 변환
    List<CourseBoardDebatePageDTO> dtoList = posts.stream().map(vo ->
        CourseBoardDebatePageDTO.builder()
            .id(vo.getId())
            .title(vo.getTitle())
            .courseName(vo.getCourseName())
            .writerName(vo.getWriterName())
            .readCount(vo.getReadCount())
            .createdAt(vo.getCreatedAt())
            .isAttached(vo.getIsAttached())
            .commentCount(vo.getCommentCount())
            .forumLike(vo.getForumLike())
            .isHotPost(vo.isHotPost())
            .approvedReportCount(vo.getApprovedReportCount())
            .build()
    ).collect(Collectors.toList());

    // 페이지네이션 정보와 함께 최종 결과 반환
    return CourseBoardDebatePagingResponseDTO.<CourseBoardDebatePageDTO>allInfo()
        .courseBoardDebatePagingRequestDTO(requestDTO)
        .dtoList(dtoList)
        .total(totalCount)
        .build();
  }

  // 상세 조회: 댓글, 좋아요 정보 추가
  @Override
  @Transactional
  public CourseBoardDebateDetailInfo getCourseBoardDebateDetail(int id, UserVO loginUser) {
    CourseBoardDebateDetailInfo detailInfo = courseBoardDebateMapper.selectCourseBoardDebateDetail(id);
    if (detailInfo == null) {
      return null;
    }

    detailInfo.setComments(courseBoardDebateMapper.findCourseBoardDebateCommentsByForumId(id));
    detailInfo.setLikeCount(courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(id));

    if (loginUser != null) {
      CourseBoardDebateLike existingLike = courseBoardDebateMapper.findCourseBoardDebateLikeByUserAndForum(id, loginUser.getId());
      detailInfo.setLikedByCurrentUser(existingLike != null);
    } else {
      detailInfo.setLikedByCurrentUser(false);
    }

    return detailInfo;
  }

  @Override
  @Transactional
  public int insertCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO) {
    courseBoardDebateMapper.insertCourseBoardDebate(courseBoardDebateDTO);
    return courseBoardDebateDTO.getId();
  }

  @Override
  @Transactional
  public boolean addCourseBoardDebateComment(CourseBoardDebateCommentDTO comment) {
    courseBoardDebateMapper.insertCourseBoardDebateComment(comment);
    int forumId = comment.getCourseForumId();
    int finalLikeCount = courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(forumId);
    int finalCommentCount = courseBoardDebateMapper.countCommentsByForumId(forumId);
    return updateHotPostStatus(forumId, finalLikeCount, finalCommentCount);
  }

  @Override
  @Transactional
  public int updateCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO) {
    return courseBoardDebateMapper.updateCourseBoardDebate(courseBoardDebateDTO);
  }

  @Override
  @Transactional
  public void deleteCourseBoardDebate(int debateId) {

    // 이 게시글에 달린 모든 댓글을 먼저 soft delete 합니다.
    courseBoardDebateMapper.deleteCommentsByForumId(debateId);

    // 댓글이 모두 삭제된 후, 원본 게시글을 soft delete 합니다.
    courseBoardDebateMapper.softDeleteCourseBoardDebateById(debateId);

  }

  @Override
  @Transactional
  public boolean updateCourseBoardDebateReadCount(ReadCountLog readCountLog) {
    int check = courseBoardDebateMapper.checkTodayReadCountLog(
        readCountLog.getTableName(),
        readCountLog.getTableId(),
        readCountLog.getUserId()
    );

    if (check == 0) {
      readCountLogMapper.insertReadCountLog(readCountLog); // 로그 삽입은 공용 매퍼 그대로 사용
      courseBoardDebateMapper.updateCourseBoardDebateReadCount(readCountLog.getTableId());
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public int toggleCourseBoardDebateLike(int forumId, int userId) {
    CourseBoardDebateLike like = new CourseBoardDebateLike();
    like.setCourseForumId(forumId);
    like.setUserId(userId);

    CourseBoardDebateLike existingLike = courseBoardDebateMapper.findCourseBoardDebateLikeByUserAndForum(forumId, userId);

    if (existingLike == null) {
      courseBoardDebateMapper.insertCourseBoardDebateLike(like);
    } else {
      courseBoardDebateMapper.deleteCourseBoardDebateLike(existingLike);
    }
    courseBoardDebateMapper.updateCourseBoardDebateLikeCount(forumId);

    int finalLikeCount = courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(forumId);
    int finalCommentCount = courseBoardDebateMapper.countCommentsByForumId(forumId);
    updateHotPostStatus(forumId, finalLikeCount, finalCommentCount);

    return courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(forumId);
  }

  @Override
  @Transactional
  public void addCourseBoardDebateReport(CourseBoardDebateReport report) {
    courseBoardDebateMapper.insertCourseBoardDebateReport(report);
  }

  @Override
  public void updateComment(int commentId, String content, UserVO loginUser) {
    CourseBoardDebateCommentDTO comment = courseBoardDebateMapper.findCommentById(commentId);

    if (comment == null) {
      throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
    }
    if (comment.getCommenterId() != loginUser.getId()) {
      throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
    }

    comment.setContent(content);
    int affectedRows = courseBoardDebateMapper.updateComment(comment);

    if (affectedRows != 1) {
      throw new RuntimeException("댓글 수정 중 오류가 발생했습니다.");
    }
  }

  @Override
  public void deleteComment(int commentId, UserVO loginUser) {
    CourseBoardDebateCommentDTO comment = courseBoardDebateMapper.findCommentById(commentId);

    if (comment == null) {
      throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
    }
    if (comment.getCommenterId() != loginUser.getId()) {
      throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
    }

    int affectedRows = courseBoardDebateMapper.softDeleteComment(commentId);

    if (affectedRows != 1) {
      throw new RuntimeException("댓글 삭제 중 오류가 발생했습니다.");
    }
    int forumId = comment.getCourseForumId();
    int finalLikeCount = courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(forumId);
    int finalCommentCount = courseBoardDebateMapper.countCommentsByForumId(forumId);
    updateHotPostStatus(forumId, finalLikeCount, finalCommentCount);
  }




  @Override
  public boolean updateHotPostStatus(int forumId, int finalLikeCount, int finalCommentCount) {
    log.info(">>>>>> 인기글 상태 업데이트 시작 (게시글 ID: {}) <<<<<<", forumId);

    boolean result = false;
    CourseBoardDebateDetailInfo postDetail = courseBoardDebateMapper.selectCourseBoardDebateDetail(forumId);
    if (postDetail == null) {
      log.warn("게시글이 존재하지 않아 업데이트를 중단합니다.");
      return false;
    }

    boolean isCurrentlyHot = postDetail.isHotPost();

    int likeCount = finalLikeCount;
    int commentCount = finalCommentCount;

    final int LIKE_THRESHOLD = 5;
    final int COMMENT_THRESHOLD = 10;

    boolean shouldBeHot = (likeCount >= LIKE_THRESHOLD && commentCount >= COMMENT_THRESHOLD);

    if (!isCurrentlyHot && shouldBeHot) {
      log.info("인기글 기준 충족. 승격 절차를 시작합니다.");
      final int HOT_POST_LIMIT = 5;
      int currentHotPostCount = courseBoardDebateMapper.countHotPosts();
      if (currentHotPostCount < HOT_POST_LIMIT) {
        log.info("인기글 자리가 남아있어 바로 승격합니다.");
        courseBoardDebateMapper.promoteToHotPost(forumId);
        sendHotPostNotification(forumId);
        result = true;
      } else {
        log.info("인기글이 꽉 차 있어, 기존 인기글과 점수 비교를 시작합니다.");
        List<CourseBoardDebateVO> hotPosts = courseBoardDebateMapper.findHotPosts();
        CourseBoardDebateVO worstHotPost = null;
        int minScore = Integer.MAX_VALUE;

        for (CourseBoardDebateVO post : hotPosts) {
          int score = post.getForumLike() + post.getCommentCount();
          if (score < minScore) {
            minScore = score;
            worstHotPost = post;
          } else if (score == minScore) {
            if (worstHotPost != null && post.getHotPostAt().isBefore(worstHotPost.getHotPostAt())) {
              worstHotPost = post;
            }
          }
        }

        if (worstHotPost != null) {
          int candidateScore = likeCount + commentCount;
          if (candidateScore > minScore) {
            log.info("승격 후보의 점수가 더 높아 교체를 진행합니다. ({} -> {})", worstHotPost.getId(), forumId);
            courseBoardDebateMapper.demoteHotPost(worstHotPost.getId());
            courseBoardDebateMapper.promoteToHotPost(forumId);
            sendHotPostNotification(forumId);
            result = true;
          } else {
            log.info("승격 후보의 점수가 기존 인기글보다 낮거나 같아 승격하지 않습니다.");
          }
        }
      }
    }
    else if (isCurrentlyHot && !shouldBeHot) {
      log.info("인기글 기준 미달. 인기글 상태를 해제합니다.");
      courseBoardDebateMapper.demoteHotPost(forumId);
    }
    else {
      log.info("상태 변경 없음. 현재 상태를 유지합니다.");
    }
    return result;
  }


  // 인기글 승격 알림을 전송하는 메소드(notificationMapper를 주입받아 사용함)
  private void sendHotPostNotification(int forumId) {
    try {
      // 게시글 ID로 작성자의 User ID를 조회합니다.
      int authorId = courseBoardDebateMapper.selectUserId(forumId);

      // 알림 내용과 링크(URI)를 설정합니다.
      List<Integer> userIds = new ArrayList<>();
      userIds.add(authorId);
      String content = "토론 게시판에 작성하신 글이 인기글로 되었습니다.";
      String targetURI = "/courseBoardDebate/debateDetail?id=" + forumId;

      NotificationSaveDTO notificationSaveDTO = NotificationSaveDTO.builder()
          .userIds(userIds)
          .content(content)
          .isWarning(false)
          .targetURI(targetURI)
          .build();
      // 실제 알림 전송 로직을 호출합니다.
      log.info("인기글 알림 전송: userId={}, content={}, targetURI={}", authorId, content, targetURI);

      if(notificationMapper.insertNotification(notificationSaveDTO) > 0){
          for(Integer userId : notificationSaveDTO.getUserIds()) {

            messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");

          }

      };

    } catch (Exception e) {
      log.error("인기글 승격 알림 전송 중 오류 발생", e);
    }
  }
  @Override
  public int getUserId(int forumId) {
    return courseBoardDebateMapper.selectUserId(forumId);
  }
}