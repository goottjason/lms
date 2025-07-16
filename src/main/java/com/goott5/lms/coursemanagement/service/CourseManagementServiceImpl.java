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

  /**
   * 전체 과정 또는 단일 과정을 조회하여 페이징 정보와 함께 반환
   * 요청받은 페이징 정보(pageNo, pageSize)가 있으면, 전체 레코드 수 먼저 조회 후, 다시 페이징조건으로 조회
   * 각 과정에는 과목 정보도 함께 세팅
   * @param commonReqDTO 공통 요청 정보 (로그인 유저 ID, 타입, 진행 여부, 과정 ID 등)
   * @param pageCourseReqDTO 과정 조회 요청 정보 (페이징, 검색 조건 등)
   * @return 페이징 정보와 과정 목록이 포함된 DTO
   */
  @Override
  public PageCourseRespDTO<CourseRespDTO> findCoursesAllorOne(
      CommonReqDTO commonReqDTO, PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO
  ) {

    // 전체 레코드 수 조회를 위해 페이징 정보를 잠시 제거
    Integer originalPageNo = pageCourseReqDTO.getPageNo();
    Integer originalPageSize = pageCourseReqDTO.getPageSize();

    // 조건에 맞는 전체 과정(혹은 단일 과정) 목록 조회
    if (originalPageNo != null && originalPageSize != null) {
      pageCourseReqDTO.setPageNo(null);
      pageCourseReqDTO.setPageSize(null);
    }

    List<CourseRespDTO> allCourses
        = courseManagementMapper.selectCoursesAllorOne(
            pageCourseReqDTO,
            commonReqDTO.getLoginUserId(),
            commonReqDTO.getLoginUserType(),
            commonReqDTO.getIsInProgress(),
            commonReqDTO.getCourseId()
    );
    int totalRecords = allCourses.size();

    // 페이징 정보가 있으면, 해당 조건으로 다시 데이터 조회
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

    // 각 과정에 과목(subjects) 정보 세팅
    if (!allCourses.isEmpty()) {
      for (CourseRespDTO course : allCourses) {
        course.setSubjects(courseManagementMapper.selectCourseSubjectById(course.getId()));
      }
    }

    // 페이징 정보 및 과정 목록을 포함한 DTO 반환
    return PageCourseRespDTO.<CourseRespDTO>withPageInfo()
        .pageCourseReqDTO(pageCourseReqDTO)
        .totalRecords(totalRecords)
        .respDTOS(allCourses)
        .build();
  }


  @Override
  public List<UserRespDTO> findEnrolledLearnersByCourseId(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Integer courseId
  ) {

    List<UserRespDTO> userRespDTOS =
        courseManagementMapper.selectEnrolledLearnersByCourseId(pageUserReqDTO, courseId);
    return userRespDTOS;
  }


  @Override
  public List<UserRespDTO> findNotEnrolledLearnersAll(
      PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      Boolean includeAll
  ) {

    List<UserRespDTO> userRespDTOS =
        courseManagementMapper.selectNotEnrolledLearnersAll(pageUserReqDTO, includeAll);

    return userRespDTOS;
  }


  @Override
  @Transactional
  public boolean addLearnerToCourse(
      Integer learnerId,
      Integer courseId) {

    // 1. 해당하는 과정의 numberOfLearner를 불러옴
    Integer numberOfLearner =
        courseManagementMapper.selectNumberOfLearnerByCoId(courseId);

    // 2. 해당하는 과정에 배정된 교육생의 수를 불러옴
    Integer enrolledLearnerCount =
        courseManagementMapper.selectErolledLearnerCount(courseId);

    int result = 0;
    if (numberOfLearner > enrolledLearnerCount) {

      // 3. numberOfLearner > enrolledLearnerCount 일때만 데이터 추가
      result = courseManagementMapper.insertLearnerToCourse(
          learnerId, courseId);

      // 4. 추가된 데이터의 leId를 조회하여 취업관리 테이블에 데이터 추가
      Integer leId = courseManagementMapper.selectLearnerEnrollmentByIds(learnerId, courseId);
      int subResult = courseManagementMapper.insertEmploymentSupport(leId);

      // 수강테이블, 취업관리테이블 모두 추가 성공시에 true 반환
      return result > 0 && subResult > 0;
    }
    return false;
  }

  @Override
  public boolean removeLearnerFromCourse(
      Integer learnerId,
      Integer courseId
  ) {

    /**
     * 수강테이블에서 learnerId, courseId에 해당하는 데이터 삭제
     * (취업관리테이블에 있는 데이터는 ON_DELETE_CASCADE임)
     */
    int result = courseManagementMapper.deleteLearnerFromCourse(learnerId, courseId);

    return result > 0;
  }

  @Transactional
  @Override
  public Boolean removeCoursesByAuth(BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest) {


    // 1. 과정의 강의실 비어있음으로 업데이트
    courseManagementMapper.updateClassroomByAuth(baseReqDTO, pageCourseRequest);
    // 2.
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

    // 1. 조회할 과정들 리스트
    List<Integer> coIds = courses.stream().map(
        CourseWithAssignedInfo::getCoId).collect(Collectors.toList());


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
    // courseList 페이지에서 요청하는 것인지?
    final boolean isCourseListPage = (referer != null && requestURI != null)
        && referer.contains("courseList") && !requestURI.contains("courseDetail");

    if (isCourseListPage) {
      subjectMap = Collections.emptyMap(); // 교과목 정보 PASS
      classDateMap = Collections.emptyMap(); // 수업일자 정보 PASS
      trainingDateMap = Collections.emptyMap(); // 훈련일지등록 정보 PASS
      learnerOverviewMap = Collections.emptyMap(); // 과정을 수강중인 교육생 정보 PASS
    } else {
      // 각 정보 조회

      // 2. 조회할 과정들의 정보들을 한번에 조회
      List<CourseSubject> subjectList =
          courseManagementMapper.selectSubjectByCoIds(coIds);
      List<CourseClassDate> classDateList =
          courseManagementMapper.selectClassDateByCoIds(coIds);
      List<LearnerOverviewResp> learnersByAuthByCoIds =
          learnerManagementService.getLearnersByAuthByCoIds(baseReqDTO, coIds);
      List<CourseTrainingDate> trainingDateList =
          courseManagementMapper.selectTrainingDateByCoIds(coIds);

      // 3. 각 과정 별로 그룹핑
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

    // 4. 각 과정에 세팅
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
  public Map<String, Integer> getIncompleteTaskCount(
      BaseReqDTO baseReqDTO, PageCourseRequest pageCourseRequest) {


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
    // 훈련일지 결재
    incompleteTaskCountMap.put(
        "trainingUnsignCount",
        courseManagementMapper.selectIncompleteSignCount(baseReqDTO, pageCourseRequest
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
      // '오늘을 포함하여 이미 종료된' 과정 조회 (혹시라도 종료되지 못한 과정 또한 종료처리하기 위함)
      if(today.isAfter(course.getCourseWithAssignedInfo().getCoEndDate())) {
        if (course.getCourseWithAssignedInfo().getCoIsInProgress()) {
          CourseWithAssignedInfo info = course.getCourseWithAssignedInfo();
          // 1. 과정 상태 업데이트
          Boolean resultByCourseStatus = modifyCourseIsInProgressByCoId(info.getCoId());
          // 2. 강의실 비활성화
          Boolean resultByClassroomStatus = operationsManagementService.modifyClassroomIsActiveBycoClassroomId(
              info.getCoClassroomId());
          // 3. 교육생 수료처리
          Boolean resultByLearnerCompletionStatus = learnerManagementService.modifyCompletionStatusByCoId(
              baseReqDTO, info.getCoId());
        }
      }
    });
  }
}
