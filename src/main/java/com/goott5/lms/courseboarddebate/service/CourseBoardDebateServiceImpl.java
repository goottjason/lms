package com.goott5.lms.courseboarddebate.service;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
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
import com.goott5.lms.learnermanagement.domain.table.User;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseBoardDebateServiceImpl implements CourseBoardDebateService {

  private final CourseBoardDebateMapper courseBoardDebateMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final CourseManagementService courseManagementService;
  private final UtilService utilService;
  private final S3Uploader s3Uploader;

  @Override
  public CourseBoardDebatePagingResponseDTO<CourseBoardDebatePageDTO> getCourseBoardDebateList(
      CourseBoardDebatePagingRequestDTO requestDTO, HttpSession session) {

    // +++ 사용자 권한에 따른 조회 범위 설정 로직 추가 시작 +++
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    // 관리자가 아닐 경우, 접근 가능한 과정 ID 목록을 설정
    if (loginUser != null && !"ADMINISTRATOR".equals(loginUser.getType())) {
      // 기존 수업자료실에서 사용하던 로직과 유사하게, 수강중인 과정 목록을 가져옵니다.
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
        // 과정 ID만 추출하여 리스트 생성
        List<Integer> accessibleCourseIds = courses.getRespDTOS().stream()
            .map(CourseRespDTO::getId)
            .collect(Collectors.toList());
        // DTO에 접근 가능 ID 리스트 설정
        requestDTO.setAccessibleCourseIds(accessibleCourseIds);
      } else {
        // 수강중인 과정이 없으면 빈 리스트를 반환하도록 처리
        return CourseBoardDebatePagingResponseDTO.<CourseBoardDebatePageDTO>allInfo()
            .courseBoardDebatePagingRequestDTO(requestDTO)
            .dtoList(new ArrayList<>())
            .total(0)
            .build();
      }
    }
    // +++ 사용자 권한에 따른 조회 범위 설정 로직 추가 끝 +++


    // Mapper에서 목록과 전체 개수를 각각 조회
    List<CourseBoardDebateVO> posts = courseBoardDebateMapper.selectCourseBoardDebateList(requestDTO);
    int totalCount = courseBoardDebateMapper.selectCourseBoardDebateTotalCount(requestDTO);

    // VO 리스트를 PageDTO 리스트로 변환 (기존 코드)
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
            .forumLike(vo.getForumLike()) // 좋아요 필드 추가
            .build()
    ).collect(Collectors.toList());

    // 페이징 정보와 DTO 리스트를 함께 담아 반환
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

    // 댓글 목록과 좋아요 수를 추가로 조회하여 DTO에 설정
    detailInfo.setComments(courseBoardDebateMapper.findCourseBoardDebateCommentsByForumId(id));
    detailInfo.setLikeCount(courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(id));

    if (loginUser != null) {
      CourseBoardDebateLike existingLike = courseBoardDebateMapper.findCourseBoardDebateLikeByUserAndForum(id, loginUser.getId());
      detailInfo.setLikedByCurrentUser(existingLike != null);
    } else {
      // 로그인하지 않은 경우, 좋아요 누르지 않은 상태로 설정
      detailInfo.setLikedByCurrentUser(false);
    }

    return detailInfo;
  }

  // 게시글 등록
  @Override
  @Transactional
  public int insertCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO) {
    // 기존 로직은 문제 없으나, 일관성을 위해 Service 인터페이스의 메소드명을 따릅니다.
    courseBoardDebateMapper.insertCourseBoardDebate(courseBoardDebateDTO);
    return courseBoardDebateDTO.getId();
  }

  // 댓글 작성: 메소드명 통일
  @Override
  @Transactional
  public void addCourseBoardDebateComment(CourseBoardDebateCommentDTO comment) {
    courseBoardDebateMapper.insertCourseBoardDebateComment(comment);
  }

  @Override
  @Transactional
  public int updateCourseBoardDebate(CourseBoardDebateDTO courseBoardDebateDTO) {
    return courseBoardDebateMapper.updateCourseBoardDebate(courseBoardDebateDTO);
  }

  @Override
  @Transactional
  public void deleteCourseBoardDebate(int debateId) {
    // 파일 삭제가 필요하다면 관련 로직 추가
    courseBoardDebateMapper.softDeleteCourseBoardDebateById(debateId);
  }

  @Override
  @Transactional
  public boolean updateCourseBoardDebateReadCount(ReadCountLog readCountLog) {
    // 중복 조회 방지 로직은 기존 수업공지 게시판의 것을 그대로 사용
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
    // 토론 게시글의 좋아요 수 업데이트 후, 최종 카운트 반환
    courseBoardDebateMapper.updateCourseBoardDebateLikeCount(forumId);
    return courseBoardDebateMapper.countCourseBoardDebateLikesByForumId(forumId);
  }

  @Override
  @Transactional
  public void addCourseBoardDebateReport(CourseBoardDebateReport report) {
    courseBoardDebateMapper.insertCourseBoardDebateReport(report);
  }

  @Override
  public void updateComment(int commentId, String content, UserVO loginUser) {
    // 수정할 댓글 정보를 가져옴
    CourseBoardDebateCommentDTO comment = courseBoardDebateMapper.findCommentById(commentId);

    // 댓글이 존재하는지, 본인이 쓴 댓글이 맞는지 확인
    if (comment == null) {
      throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
    }
    if (comment.getCommenterId() != loginUser.getId()) {
      throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
    }

    // 수정할 내용을 DTO에 담아 매퍼로 전달
    comment.setContent(content);
    int affectedRows = courseBoardDebateMapper.updateComment(comment);

    if (affectedRows != 1) {
      // 업데이트가 제대로 되지 않았을 경우 예외 발생
      throw new RuntimeException("댓글 수정 중 오류가 발생했습니다.");
    }
  }

  @Override
  public void deleteComment(int commentId, UserVO loginUser) {
    // 1. 삭제할 댓글 정보를 가져옴 (재사용)
    CourseBoardDebateCommentDTO comment = courseBoardDebateMapper.findCommentById(commentId);

    // 2. 댓글이 존재하는지, 본인이 쓴 댓글이 맞는지 확인
    if (comment == null) {
      throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
    }
    if (comment.getCommenterId() != loginUser.getId()) {
      throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
    }

    // 3. 삭제(soft delete) 처리
    int affectedRows = courseBoardDebateMapper.softDeleteComment(commentId);

    if (affectedRows != 1) {
      throw new RuntimeException("댓글 삭제 중 오류가 발생했습니다.");
    }
  }

}
