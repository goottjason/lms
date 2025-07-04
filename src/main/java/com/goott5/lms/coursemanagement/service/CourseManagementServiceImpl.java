package com.goott5.lms.coursemanagement.service;

import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.domain.dto.CourseClassDate;
import com.goott5.lms.coursemanagement.domain.dto.CourseTrainingDate;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseRequest;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseResponse;
import com.goott5.lms.coursemanagement.domain.integrated.CourseInstructorOverviewResp;
import com.goott5.lms.coursemanagement.domain.integrated.CourseLearnerOverviewResp;
import com.goott5.lms.coursemanagement.domain.integrated.CourseOverviewResp;
import com.goott5.lms.coursemanagement.domain.integrated.CourseScheduleOverviewResp;
import com.goott5.lms.coursemanagement.domain.integrated.CourseSubjectOverviewResp;
import com.goott5.lms.coursemanagement.domain.integrated.InstructorOverviewResp;
import com.goott5.lms.coursemanagement.domain.table.CourseSchedule;
import com.goott5.lms.coursemanagement.domain.table.CourseSubject;
import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.coursemanagement.mapper.CourseManagementMapper;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import com.goott5.lms.learnermanagement.domain.dto.LearnerResponse;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerResponse;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import com.goott5.lms.learnermanagement.mapper.LearnerManagementMapper;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.service.OperationsManagementService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseManagementServiceImpl implements CourseManagementService {

  private final CourseManagementMapper courseManagementMapper;
  private final LearnerManagementService learnerManagementService;
  private final OperationsManagementService operationsManagementService;
  private final LearnerManagementMapper learnerManagementMapper;

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

    // 페이징된 데이터 결과가 있으면, 배열을 순회하면서 교과목 정보 set
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
  public Boolean removeCoursesByAuth(BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest) {

    // learner_enrollment, staff_assignment, course_allocation는 CASCADE 삭제

    // classroom의 is_active 값 0으로 업데이트
    courseManagementMapper.updateClassroomByAuth(baseReqDTO, pageCourseRequest);

    return courseManagementMapper.deleteCourseByAuth(baseReqDTO, pageCourseRequest);
  }


  public PageCourseResponse<CourseOverviewResp> getCoursesByAuth(
      BaseReqDTO baseReqDTO,
      PageCourseRequest pageCourseRequest,
      HttpServletRequest request
  ) {
    // 카운트 쿼리
    Integer totalRecords = courseManagementMapper.selectCountCoursesByAuth(
        baseReqDTO, toCountRequest(pageCourseRequest));

    // 데이터 쿼리
    List<CourseWithAssignedInfo> courses = courseManagementMapper.selectCoursesByAuth(
        baseReqDTO, pageCourseRequest);

    // coId 리스트
    List<Integer> coIds = courses.stream().map(
        CourseWithAssignedInfo::getCoId).collect(Collectors.toList());

    log.info(coIds.toString());

    Map<Integer, List<CourseSubject>> subjectMap;
    Map<Integer, List<CourseClassDate>> classDateMap;
    Map<Integer, List<LearnerOverviewResp>> learnerOverviewMap;
    Map<Integer, List<CourseTrainingDate>> trainingDateMap;

    String referer = null;
    String requestURI = null;

    if (request != null) {
      referer = request.getHeader("referer");
      requestURI = request.getRequestURI();
    }
    log.info("■■■referer: {}", referer);
    log.info("■■■requestURI: {}", requestURI);

    final boolean isCourseListPage = (referer != null && requestURI != null)
        && referer.contains("courseList") && !requestURI.contains("courseDetail");

    if (isCourseListPage) {
      subjectMap = Collections.emptyMap();
      classDateMap = Collections.emptyMap();
      trainingDateMap = Collections.emptyMap();
      learnerOverviewMap = Collections.emptyMap();
    } else {

      List<CourseSubject> subjectList =
          courseManagementMapper.selectSubjectByCoIds(coIds);
      List<CourseClassDate> classDateList =
          courseManagementMapper.selectClassDateByCoIds(coIds);
      List<LearnerOverviewResp> learnersByAuthByCoIds = learnerManagementService.getLearnersByAuthByCoIds(
          baseReqDTO, coIds);
      List<CourseTrainingDate> trainingDateList =
          courseManagementMapper.selectTrainingDateByCoIds(coIds);
      subjectMap = subjectList.stream()
          .collect(Collectors.groupingBy(CourseSubject::getCuCourseId));
      classDateMap = classDateList.stream()
          .collect(Collectors.groupingBy(CourseClassDate::getCsCourseId));
      learnerOverviewMap = learnersByAuthByCoIds.stream()
          .collect(Collectors.groupingBy(LearnerOverviewResp::getLeCourseId));
      trainingDateMap = trainingDateList.stream()
          .collect(Collectors.groupingBy(CourseTrainingDate::getTlCourseId));
    }

    LocalDate today = LocalDate.now();

    List<CourseOverviewResp> courseOverviewResps =
        courses.stream().map(course -> {
          return buildCourseOverviewResp(
              course,
              subjectMap,
              classDateMap,
              learnerOverviewMap,
              trainingDateMap,
              today,
              isCourseListPage);
        }).collect(Collectors.toList());

    return PageCourseResponse.<CourseOverviewResp>withPageInfo()
        .request(pageCourseRequest)
        .totalRecords(totalRecords)
        .records(courseOverviewResps)
        .build();
  }

  private CourseOverviewResp buildCourseOverviewResp (
      CourseWithAssignedInfo course,
      Map<Integer, List<CourseSubject>> subjectMap,
      Map<Integer, List<CourseClassDate>> classDateMap,
      Map<Integer, List<LearnerOverviewResp>> learnerOverviewMap,
      Map<Integer, List<CourseTrainingDate>> trainingDateMap,
      LocalDate today,
      Boolean isCourseListPage
  ) {
    CourseOverviewResp resp = new CourseOverviewResp();

    if (course.getCoId() == null) { return resp; }

    // 1. 과정 정보 추가
    resp.setCourseWithAssignedInfo(course);

    if (isCourseListPage) {
      return resp;
    }
    // 2. 과정의 교과목 및 교재 정보 추가
    List<CourseSubject> courseSubjectListByCoId
        = subjectMap.getOrDefault(course.getCoId(), Collections.emptyList());

    resp.setSubjectOverview(
        CourseSubjectOverviewResp.<CourseSubject>builder()
            .subjectList(courseSubjectListByCoId)
            .totalCount(courseSubjectListByCoId.size())
            .build()
    );
    // 3. 과정의 수업일자와 현재 과정진행률 정보 추가
    List<LocalDate> classDateList = classDateMap.getOrDefault(
            course.getCoId(), Collections.emptyList())
        .stream()
        .map(CourseClassDate::getCsClassDate)
        .collect(Collectors.toList());

    Integer progressedCount = 0;
    for (LocalDate courseClassDate : classDateList) {
      if (courseClassDate.isBefore(today)) {
        progressedCount++;
      }
    }
    // 과정진행률 (현재 날짜의 전일 기준, xx%)
    Double courseProgressRate =
        Math.round((progressedCount / (double) course.getCoTotalDays()) * 100.0 * 100.0) / 100.0;

    resp.setScheduleOverview(
        CourseScheduleOverviewResp.<CourseSchedule>builder()
            .scheduleList(null)
            .classdateList(classDateList)
            .totalCount(classDateList.size())
            .courseProgressRate(courseProgressRate)
            .build()
    );

    // 4. 과정의 교육생 전체 정보 추가
    List<LearnerOverviewResp> learnerOverviewResps
        = learnerOverviewMap.getOrDefault(course.getCoId(),
        Collections.emptyList());
    Integer denominator = learnerOverviewResps.size();
    Double numerator = 0.0;
    Double courseAvgAttendanceRate = 0.0;

    if (learnerOverviewResps.size() > 0) {
      for (LearnerOverviewResp record : learnerOverviewResps) {
        numerator += record.getPartOverview().getAttendanceRate();
      }
      courseAvgAttendanceRate = Math.round((numerator/denominator) * 100.0) / 100.0;
    }
    resp.setCourseLearnerOverview(
        CourseLearnerOverviewResp.<LearnerOverviewResp>builder()
            .learnerList(learnerOverviewResps)
            .totalCount(learnerOverviewResps.size())
            .courseAvgAttendanceRate(courseAvgAttendanceRate)
            .build()
    );

    // 5. 과정의 강사의 훈련일지 정보 추가
    List<LocalDate> trainingDateListByCoId = trainingDateMap.getOrDefault(
            course.getCoId(), Collections.emptyList())
        .stream()
        .map(CourseTrainingDate::getTlTrainingDate)
        .collect(Collectors.toList());
    resp.setCourseTrainingDates(trainingDateListByCoId);

    return resp;
  }

  private PageCourseRequest toCountRequest(PageCourseRequest req) {
    return req.toBuilder()
        .pageNo(null)
        .pageSize(null)
        .build();
  }


  private CourseScheduleOverviewResp<CourseSchedule> fetchScheduleOverview(Integer coId, Integer totalDays) {
    /*List<CourseSchedule> details =
        courseManagementMapper.selectScheduleByCoId(coId);*/

    List<LocalDate> classDates = courseManagementMapper.selectClassDateByCoId(coId);
    // Set<LocalDate> classDates = new LinkedHashSet<>(); // 순서 유지, 중복 제거

    LocalDate today = LocalDate.now();
    Integer progressedCount = 0;
    /*for (CourseSchedule detail : details) {
      classDates.add(detail.getCsClassDate());
    }*/
    for (LocalDate classDate : classDates) {
      if (classDate.isBefore(today)) {
        progressedCount++;
      }
    }
    // 과정진행률 (현재 날짜의 전일 기준, xx%)
    Double courseProgressRate =
        Math.round((progressedCount / (double) totalDays) * 100.0 * 100.0) / 100.0;

    return CourseScheduleOverviewResp.<CourseSchedule>builder()
        .scheduleList(null)
        .classdateList(classDates)
        .totalCount(classDates.size())
        .courseProgressRate(courseProgressRate)
        .build();
  }
  private CourseSubjectOverviewResp<CourseSubject> fetchSubjectOverview(Integer coId) {
    List<CourseSubject> details =
        courseManagementMapper.selectSubjectByCoId(coId);


    return CourseSubjectOverviewResp.<CourseSubject>builder()
        .subjectList(details)
        .totalCount(details.size())
        .build();
  }
  private CourseLearnerOverviewResp<LearnerOverviewResp> fetchLearnerOverview(
      BaseReqDTO baseReqDTO,
      Integer coId) {
      PageLearnerRequest pageLearnerRequest = PageLearnerRequest.builder()
          .pageNo(null)
          .pageSize(null)
          .type("userFullname")
          .keyword(null)
          .orderBy("userFullname")
          .orderDirection("ASC")
          .coIsInProgress(null)
          .leCourseId(coId)
          .leId(null)
          .build();

    PageLearnerResponse<LearnerOverviewResp> learnersWithPagination =
        learnerManagementService.getLearnersByAuth(baseReqDTO, pageLearnerRequest);

    List<LearnerOverviewResp> records = learnersWithPagination.getRecords();
    Integer denominator = records.size();
    Double numerator = 0.0;
    Double courseAvgAttendanceRate = 0.0;

    if (records.size() > 0) {
      for (LearnerOverviewResp record : records) {
        numerator += record.getPartOverview().getAttendanceRate();
      }
      courseAvgAttendanceRate = Math.round((numerator/denominator) * 100.0) / 100.0;
    }

    return CourseLearnerOverviewResp.<LearnerOverviewResp>builder()
        .learnerList(records)
        .totalCount(records.size())
        .courseAvgAttendanceRate(courseAvgAttendanceRate)
        .build();
  }
  private List<LocalDate> fetchCourseTrainingDates(Integer coId) {
    return courseManagementMapper.selectCourseTrainingDates(coId);
  }


  @Override
  public Boolean modifyCourseIsInProgressByCoId(Integer coId) {
    return courseManagementMapper.modifyCourseIsInProgressByCoId(coId);
  }

  @Override
  public Map<String, Integer> getIncompleteTaskCount(BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest) {

    Map<String, Integer> incompleteTaskCountMap = new HashMap<String, Integer>();
    // 1:1문의
    incompleteTaskCountMap.put(
        "inquiryCount",
        courseManagementMapper.selectIncompleteInquiryCount(baseReqDTO, pageCourseRequest
        )
    );
    // 게시글 신고
    incompleteTaskCountMap.put(
        "forumReportCount",
        courseManagementMapper.selectIncompleteReportCount(baseReqDTO, pageCourseRequest
        )
    );
    return incompleteTaskCountMap;
  }

  @Transactional
  @Override
  public void endCoursesAutoProcess() {

    HttpServletRequest request = null;
    LocalDate today = LocalDate.now();

    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(33)
        .loginUserType("ADMINISTRATOR")
        .loginUserPosition("GENERAL_MANAGER")
        .build();
    PageCourseRequest pageCourseRequest = PageCourseRequest.builder().build();



    PageCourseResponse<CourseOverviewResp> coursesWithPagination =
        getCoursesByAuth(baseReqDTO, pageCourseRequest, request);



    coursesWithPagination.getRecords().forEach(course -> {
      // 오늘을 포함하여 이미 종료된 과정 조회
      if(today.isAfter(course.getCourseWithAssignedInfo().getCoEndDate())) {
        if (course.getCourseWithAssignedInfo().getCoIsInProgress()) {
          log.info("오늘을 포함하여 이미 종료된 과정: {} (종료일: {})",
              course.getCourseWithAssignedInfo().getCoName(),
              course.getCourseWithAssignedInfo().getCoEndDate());

          CourseWithAssignedInfo info = course.getCourseWithAssignedInfo();
          // 오늘 종료된 과정은 종료처리 (혹시라도 종료되지 못한 과정 또한 종료처리)

          // 과정 상태 업데이트
          Boolean result1 = modifyCourseIsInProgressByCoId(info.getCoId());
          log.info("과정상태 업데이트 완료");

          // 강의실 비활성화
          Boolean result2 = operationsManagementService.modifyClassroomIsActiveBycoClassroomId(
              info.getCoClassroomId());
          log.info("강의실 비활성화 완료");

          // 교육생 수료처리
          Boolean result3 = learnerManagementService.modifyCompletionStatusByCoId(
              baseReqDTO, info.getCoId());
          log.info("교육생 수료 또는 중도탈퇴 처리 완료");
        }
      }
    });
  }
}
