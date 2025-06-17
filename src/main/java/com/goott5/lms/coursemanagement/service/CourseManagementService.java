package com.goott5.lms.coursemanagement.service;

import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import java.util.List;

public interface CourseManagementService {

  /**
   * 전체 과정 리스트 조회 API
   *
   * @param pageCourseReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param isInProgress
   * @return
   */
  PageCourseRespDTO<CourseRespDTO> findCoursesAll(
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO,
      Integer loginUserId,
      String loginUserType,
      Boolean isInProgress);

  /**
   * 과정 상세 조회 API
   *
   * @param loginUserId
   * @param loginUserType
   * @param courseId
   * @return
   */
  CourseRespDTO findCourse(Integer loginUserId, String loginUserType, Integer courseId);

  /**
   * 교육생 배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param courseId
   * @return
   */
  List<UserRespDTO> findEnrolledLearnersByCourseId(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Integer courseId);

  /**
   * 교육생 미배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param includeAll
   * @return
   */
  List<UserRespDTO> findNotEnrolledLearnersAll(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Boolean includeAll);

  /**
   * 교육생 배정 '추가' API
   *
   * @param loginUserId
   * @param loginUserType
   * @param learnerId
   * @param courseId
   * @return
   */
  boolean addLearnerToCourse(Integer loginUserId, String loginUserType, Integer learnerId,
      Integer courseId);

  /**
   * 교육생 배정 '삭제' API
   *
   * @param loginUserId
   * @param loginUserType
   * @param learnerId
   * @param courseId
   * @return
   */
  boolean removeLearnerFromCourse(Integer loginUserId, String loginUserType, Integer learnerId,
      Integer courseId);

  /**
   * 과정 삭제 API
   *
   * @param commonReqDTO
   * @return
   */
  Boolean removeCourse(CommonReqDTO commonReqDTO);
}
