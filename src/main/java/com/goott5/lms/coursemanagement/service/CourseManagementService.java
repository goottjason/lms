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


  List<UserRespDTO> findEnrolledLearnersByCourseId(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Integer courseId);

  List<UserRespDTO> findNotEnrolledLearnersAll(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Boolean includeAll);


  boolean addLearnerToCourse(Integer learnerId,
      Integer courseId);


  boolean removeLearnerFromCourse(Integer learnerId, Integer courseId);

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
