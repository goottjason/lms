package com.goott5.lms.coursemanagement.service;

import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseGetReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseRequest;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseResponse;
import com.goott5.lms.coursemanagement.domain.integrated.CourseOverviewResp;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface CourseManagementService {

  PageCourseRespDTO<CourseRespDTO> findCoursesAllorOne(
      CommonReqDTO commonReqDTO, PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO
  );


  /*CourseRespDTO findCourse(Integer loginUserId, String loginUserType, Integer courseId);*/

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


  /*Boolean removeCourse(CommonReqDTO commonReqDTO);*/

  PageCourseResponse<CourseOverviewResp> getCoursesByAuth(
      BaseReqDTO baseReqDTO,
      PageCourseRequest pageCourseRequest,
      HttpServletRequest request
  );

  Boolean removeCoursesByAuth(BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest);

  Boolean modifyCourseIsInProgressByCoId(Integer coId);

  Map<String, Integer> getIncompleteTaskCount(
      BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest
  );

  void endCoursesAutoProcess();
}
