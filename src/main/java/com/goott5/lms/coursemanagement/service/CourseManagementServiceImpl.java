package com.goott5.lms.coursemanagement.service;

import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseGetReqDTO;
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

  @Override
  public PageCourseRespDTO<CourseRespDTO> findCoursesAllorOne(
      CommonReqDTO commonReqDTO, PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO
  ) {
    // page정보를 임시 변수에 저장
    Integer originalPageNo = pageCourseReqDTO.getPageNo();
    Integer originalPageSize = pageCourseReqDTO.getPageSize();

    // page정보가 있으면, 임시로 null로 바꿈
    if (originalPageNo != null && originalPageSize != null) {
      pageCourseReqDTO.setPageNo(null);
      pageCourseReqDTO.setPageSize(null);
    }

    // 전체의 레코드 개수를 셈
    List<CourseRespDTO> allCourses
        = courseManagementMapper.selectCoursesAllorOne(
            pageCourseReqDTO,
            commonReqDTO.getLoginUserId(),
            commonReqDTO.getLoginUserType(),
            commonReqDTO.getIsInProgress(),
            commonReqDTO.getCourseId()
    );
    int totalRecords = allCourses.size();

    // page정보가 있으면, 임시 변수에서 기존의 page 정보를 꺼내어 페이징된 데이터만 조회
    if (originalPageNo != null && originalPageSize != null) {
      pageCourseReqDTO.setPageNo(originalPageNo);
      pageCourseReqDTO.setPageSize(originalPageSize);
      allCourses
          = courseManagementMapper.selectCoursesAllorOne(
              pageCourseReqDTO,
              commonReqDTO.getLoginUserId(),
              commonReqDTO.getLoginUserType(),
              commonReqDTO.getIsInProgress(),
              commonReqDTO.getCourseId()
      );
    }

    // 페이징된 데이터 결과가 있으면, 배열을 순회하면서
    if (!allCourses.isEmpty()) {
      for (CourseRespDTO course : allCourses) {
        course.setSubjects(courseManagementMapper.selectCourseSubjectById(course.getId()));
      }
    }

    return PageCourseRespDTO.<CourseRespDTO>withPageInfo()
        .pageCourseReqDTO(pageCourseReqDTO)
        .totalRecords(totalRecords)
        .respDTOS(allCourses)
        .build();
  }

  /*@Override
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
  }*/

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
    PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = new PageCourseReqDTO<CourseReqDTO>();

    List<CourseRespDTO> courses = courseManagementMapper.selectCoursesAllorOne(
        pageCourseReqDTO, loginUserId, loginUserType, null, courseId);

    // 해당하는 과정에 수강중인 교육생의 수를 불러옴(B)
    Integer enrolledLernerCount = courseManagementMapper.selectErolledLernerCount(courseId);
    int result = 0;
    // A>B 일때만 추가가능
    if (courses.get(0).getNumberOfLearner() > enrolledLernerCount) {
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

    PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = new PageCourseReqDTO<CourseReqDTO>();

    List<CourseRespDTO> courses =
        courseManagementMapper.selectCoursesAllorOne(
            pageCourseReqDTO,
            commonReqDTO.getLoginUserId(),
            commonReqDTO.getLoginUserType(),
            null,
            commonReqDTO.getCourseId()
        );
    CourseRespDTO courseRespDTO = courseManagementMapper.selectCourse(
        commonReqDTO.getLoginUserId(),
        commonReqDTO.getLoginUserType(),
        commonReqDTO.getCourseId());
    LocalDate today = LocalDate.now();

    if (courses != null) {
      if (courses.get(0).getStartDate().isAfter(today)) {
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
