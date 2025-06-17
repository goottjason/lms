package com.goott5.lms.coursemanagement.service;

import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.mapper.CourseManagementMapper;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseManagementServiceImpl implements CourseManagementService {

  private final CourseManagementMapper courseManagementMapper;

  /**
   * 전체 과정 리스트 조회 API
   *
   * @param pageCourseReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param isInProgress
   * @return
   */
  @Override
  public PageCourseRespDTO<CourseRespDTO> findCoursesAll(
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO,
      Integer loginUserId,
      String loginUserType,
      Boolean isInProgress
  ) {

    Integer originalPageNo = pageCourseReqDTO.getPageNo();
    Integer originalPageSize = pageCourseReqDTO.getPageSize();

    if (originalPageNo != null && originalPageSize != null) {
      pageCourseReqDTO.setPageNo(null);
      pageCourseReqDTO.setPageSize(null);
    }

    List<CourseRespDTO> allCourses = courseManagementMapper.selectCoursesAll(
        pageCourseReqDTO, loginUserId, loginUserType, isInProgress);
    int totalRecords = allCourses.size();

    if (originalPageNo != null && originalPageSize != null) {
      pageCourseReqDTO.setPageNo(originalPageNo);
      pageCourseReqDTO.setPageSize(originalPageSize);
      // 페이징된 데이터만 조회
      allCourses = courseManagementMapper.selectCoursesAll(
          pageCourseReqDTO, loginUserId, loginUserType, isInProgress);
    }

    if (!allCourses.isEmpty()) {
      for (CourseRespDTO courseRespDTO : allCourses) {
        String instructorFullname = courseManagementMapper.selectInstructorFullname(courseRespDTO);
        if (instructorFullname == null) {
          instructorFullname = "미배정";
        }
        courseRespDTO.setInstructorFullname(instructorFullname);
        String classroomName = courseManagementMapper.selectClassroomName(courseRespDTO);
        if (classroomName == null) {
          classroomName = "미배정";
        }
        courseRespDTO.setClassroomName(classroomName);
      }
    }

    return PageCourseRespDTO.<CourseRespDTO>withPageInfo()
        .pageCourseReqDTO(pageCourseReqDTO)
        .totalRecords(totalRecords)
        .respDTOS(allCourses)
        .build();
  }

  /**
   * 과정 상세 조회 API
   *
   * @param loginUserId
   * @param loginUserType
   * @param courseId
   * @return
   */
  @Override
  public CourseRespDTO findCourse(
      Integer loginUserId,
      String loginUserType,
      Integer courseId
  ) {

    CourseRespDTO course =
        courseManagementMapper.selectCourse(loginUserId, loginUserType, courseId);

    if (course != null) {
      String instructorFullname = courseManagementMapper.selectInstructorFullname(course);
      if (instructorFullname == null) {
        instructorFullname = "미배정";
      }
      course.setInstructorFullname(instructorFullname);
      String classroomName = courseManagementMapper.selectClassroomName(course);
      if (classroomName == null) {
        classroomName = "미배정";
      }
      course.setClassroomName(classroomName);
      course.setSubjects(courseManagementMapper.selectCourseSubjectById(courseId));
    }

    return course;
  }

  /**
   * 교육생 배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param courseId
   * @return
   */
  @Override
  public List<UserRespDTO> findEnrolledLearnersByCourseId(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Integer courseId
  ) {

    List<UserRespDTO> userRespDTOS =
        courseManagementMapper.selectEnrolledLearnersByCourseId(pageUserReqDTO, courseId);
    log.info(userRespDTOS.toString());
    return userRespDTOS;
  }

  /**
   * 교육생 미배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param includeAll
   * @return
   */
  @Override
  public List<UserRespDTO> findNotEnrolledLearnersAll(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Boolean includeAll
  ) {

    List<UserRespDTO> userRespDTOS =
        courseManagementMapper.selectNotEnrolledLearnersAll(pageUserReqDTO, includeAll);

    return userRespDTOS;
  }

  /**
   * 교육생 배정 '추가' API
   *
   * @param loginUserId
   * @param loginUserType
   * @param learnerId
   * @param courseId
   * @return
   */
  @Override
  public boolean addLearnerToCourse(
      Integer loginUserId,
      String loginUserType,
      Integer learnerId,
      Integer courseId) {
    // 해당하는 과정의 총원을 불러옴(A)
    CourseRespDTO course = courseManagementMapper.selectCourse(loginUserId, loginUserType,
        courseId);
    // 해당하는 과정에 수강중인 교육생의 수를 불러옴(B)
    Integer enrolledLernerCount = courseManagementMapper.selectErolledLernerCount(courseId);
    log.info("총원: {}, 현재수강인원: {}", course.getNumberOfLearner(), enrolledLernerCount);
    int result = 0;
    // A>B 일때만 추가가능
    if (course.getNumberOfLearner() > enrolledLernerCount) {
      result = courseManagementMapper.insertLearnerToCourse(learnerId, courseId);
      Integer leId = courseManagementMapper.selectLearnerEnrollmentByIds(learnerId, courseId);
      int subResult = courseManagementMapper.insertEmploymentSupport(leId);
      return result > 0 && subResult > 0;
    }

    return false;
  }

  /**
   * 교육생 배정 '삭제' API
   *
   * @param loginUserId
   * @param loginUserType
   * @param learnerId
   * @param courseId
   * @return
   */
  @Override
  public boolean removeLearnerFromCourse(
      Integer loginUserId,
      String loginUserType,
      Integer learnerId,
      Integer courseId
  ) {

    int result = courseManagementMapper.deleteLearnerFromCourse(learnerId, courseId);
    return result > 0;
  }


  @Override
  public Boolean removeCourse(CommonReqDTO commonReqDTO) {
    CourseRespDTO courseRespDTO = courseManagementMapper.selectCourse(
        commonReqDTO.getLoginUserId(),
        commonReqDTO.getLoginUserType(),
        commonReqDTO.getCourseId());
    LocalDate today = LocalDate.now();

    if (courseRespDTO != null) {
      if (courseRespDTO.getStartDate().isAfter(today)) {
        log.info("과정시작일이 오늘 이후임");
        int result = courseManagementMapper.deleteCourse(
            commonReqDTO.getLoginUserId(),
            commonReqDTO.getLoginUserType(),
            commonReqDTO.getCourseId()
        );
        return result > 0;
      }
    }
    return false;
  }


}
