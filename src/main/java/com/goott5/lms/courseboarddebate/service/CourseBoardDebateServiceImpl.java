package com.goott5.lms.courseboarddebate.service;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.courseboarddebate.domain.*;
import com.goott5.lms.courseboarddebate.mapper.CourseBoardDebateMapper;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseBoardDebateServiceImpl implements CourseBoardDebateService {

  private final CourseBoardDebateMapper courseBoardDebateMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final CourseManagementService courseManagementService;


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

    // 최종적으로 화면에 보여줄 글 목록 (인기글 + 일반글)
    List<CourseBoardDebateVO> combinedList = new ArrayList<>();

    // 첫 페이지(pageNo=1)일 경우에만 인기글 목록을 조회해서 리스트에 먼저 추가합니다.
    if (requestDTO.getPageNo() == 1) {
      List<CourseBoardDebateVO> hotPosts = courseBoardDebateMapper.selectHotPosts(requestDTO);
      if (hotPosts != null) {
        combinedList.addAll(hotPosts);
      }
    }

    // 페이지네이션 계산을 위한 "일반글"의 전체 개수를 조회합니다.
    int regularPostsTotalCount = courseBoardDebateMapper.selectRegularPostsTotalCount(requestDTO);

    // 현재 페이지에 해당하는 "일반글" 목록을 조회합니다.
    List<CourseBoardDebateVO> regularPosts = courseBoardDebateMapper.selectRegularPosts(requestDTO);
    if (regularPosts != null) {
      combinedList.addAll(regularPosts);
    }

    // 조회된 VO 리스트를 화면에 보여줄 PageDTO 리스트로 변환합니다.
    List<CourseBoardDebatePageDTO> dtoList = combinedList.stream().map(vo ->
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

    // 페이지네이션 정보는 "일반글"의 전체 개수를 기준으로 생성하여 최종 반환합니다.
    return CourseBoardDebatePagingResponseDTO.<CourseBoardDebatePageDTO>allInfo()
        .courseBoardDebatePagingRequestDTO(requestDTO)
        .dtoList(dtoList) // 화면에 보여줄 목록은 인기글과 일반글이 합쳐진 리스트
        .total(regularPostsTotalCount) // 페이지네이션 계산 기준은 일반글의 총 개수
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
    courseBoardDebateMapper.softDeleteCourseBoardDebateById(debateId);
  }

  @Override
  @Transactional
  public boolean updateCourseBoardDebateReadCount(ReadCountLog readCountLog) {
    int check = readCountLogMapper.checkReadCountLog(readCountLog.getTableName(), readCountLog.getTableId(), readCountLog.getUserId());
    if (check == 0) {
      readCountLogMapper.insertReadCountLog(readCountLog);
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

  @Override
  public int getUserId(int forumId) {
    return courseBoardDebateMapper.selectUserId(forumId);
  }
}