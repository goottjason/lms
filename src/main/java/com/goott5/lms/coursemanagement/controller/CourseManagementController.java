package com.goott5.lms.coursemanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseRequest;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseResponse;
import com.goott5.lms.coursemanagement.domain.integrated.CourseOverviewResp;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CourseManagementController {

  // ========== 서비스 주입
  private final CourseManagementService courseManagementService;
  private final LearnerManagementService learnerManagementService;

  // ========== 페이지 렌더링

  /**
   * 관리자 홈 페이지 반환
   * @return 관리자 홈 페이지의 뷰 이름
   */
  @GetMapping("/home/administratorHome")
  public String administratorHome() {
    return "home/administratorHome";
  }

  /**
   * 과정 조회 페이지 반환
   * @return 과정 조회 페이지의 뷰 이름
   */
  @GetMapping("/courseManagement/courseList")
  public String courseList() {
    return "courseManagement/courseList";
  }

  /**
   * 과정 상세 페이지 반환
   * 주어진 과정의 ID(courseId)에 해당하는 과정 상세 정보를 조회하여
   * 모델에 추가하고, 상세 페이지 뷰를 반환
   * 쿼리스트링이 없는 경우, 진행중인 첫 번째 과정을 조회
   * @param coId 과정 ID(courseId)
   * @param model 뷰에 데이터를 전달하기 위한 Model 객체
   * @param session 현재 로그인한 사용자의 세션을 전달하기 위한 HttpSession 객체
   * @param request HTTP 요청 정보를 전달하기 위한 HttpServletRequest 객체
   * @return 과정 상세 페이지의 뷰 이름
   */
  @GetMapping("/courseManagement/courseDetail")
  public String courseDetail(
      @RequestParam(value = "courseId", defaultValue = "-1") Integer coId,
      Model model,
      HttpSession session,
      HttpServletRequest request
  ) {

    // 로그인유저 정보
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();

    // baseReqDTO 객체 생성
    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(loginUserId)
        .loginUserType(loginUserType)
        .build();

    // 로그인유저 포지션 요청 후 baseReqDTO 객체에 세팅
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    // pageCourseRequest 객체 생성
    PageCourseRequest pageCourseRequest = PageCourseRequest.builder()
        .pageNo(null)
        .pageSize(null)
        .type("coName")
        .keyword(null)
        .orderBy("coStartDate")
        .orderDirection("ASC")
        .coIsInProgress(null)
        .coId(coId)
        .build();

    // 쿼리스트링 없이 요청(강사 및 교육생의 경우, 사이드바에서 접근할 때)
    if (coId == -1) {
      // (쿼리문에서 정렬기준대로) 진행중-종료 순서, 과정시작일 빠른 순서로 조회되는 첫번째 과정
      pageCourseRequest.setCoId(null);
      PageCourseResponse<CourseOverviewResp> coursesWithPagination =
          courseManagementService.getCoursesByAuth(baseReqDTO, pageCourseRequest, request);
    }

    // 주어진 요청 정보와 사용자 정보를 바탕으로, 권한에 맞는 과정 목록을 조회
    PageCourseResponse<CourseOverviewResp> coursesWithPagination =
        courseManagementService.getCoursesByAuth(baseReqDTO, pageCourseRequest, request);

    // 조회된 과정 목록 중 첫 번째 과정을 모델에 "record"라는 이름으로 추가
    model.addAttribute("record", coursesWithPagination.getRecords().get(0));

    return "courseManagement/courseDetail";
  }

  /**
   * 교육생 배정 페이지 반환
   * @param session 현재 로그인한 사용자의 세션 정보를 담고 있는 HttpSession 객체
   * @param model 뷰에 데이터를 전달하기 위한 Model 객체
   * @return 교육생 배정 페이지의 뷰 이름
   */
  @GetMapping("/courseManagement/learnerAssignment")
  public String learnerAssignment(HttpSession session, Model model) {
    model.addAttribute("loginUser", session.getAttribute("loginUser"));
    return "courseManagement/learnerAssignment";
  }

  // ========== API

  /**
   * 전체 과정 또는 단일 과정을 조회하여 반환
   * 공통 요청 정보와 과정 조회 요청 정보를 받아, 조건에 맞는 과정 목록(혹은 단일 과정)을 조회
   * 결과는 ApiResponse로 감싼 후 ResponseEntity로 반환
   * @param commonReqDTO 공통 요청 정보 (예: 로그인 유저 정보, 권한 등)
   * @param pageCourseReqDTO 과정 조회 요청 정보 (예: 페이징, 검색 조건 등)
   * @return 과정 목록 또는 단일 과정 정보를 담은 ApiResponse의 ResponseEntity
   */
  @GetMapping("/api/management/courses")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseRespDTO<CourseRespDTO>>> getCoursesAllorOne(
      @ModelAttribute CommonReqDTO commonReqDTO,
      @ModelAttribute PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO
  ) {

    // 요청 조건에 따라 전체 과정 또는 단일 과정을 조회
    PageCourseRespDTO<CourseRespDTO> courses =
        courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

    // 조회 결과를 ApiResponse로 감싸서 반환
    return ApiResponse.okResponse(200, "success", courses);
  }

  /**
   * 과정에 배정된 교육생 목록을 조회
   * @param pageUserReqDTO 페이징 및 검색 조건을 담은 요청 DTO
   * @param courseId 교육생 목록을 조회할 과정 ID
   * @return 배정된 교육생 정보 리스트
   */
  @GetMapping("/api/learners/enrolled")
  @ResponseBody
  public List<UserRespDTO> getEnrolledLearnersByCourseId(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam("courseId") Integer courseId
  ) {
    // 과정에 배정된 교육생의 UserRespDTO를 담은 리스트를 반환
    return courseManagementService.findEnrolledLearnersByCourseId(pageUserReqDTO, courseId);
  }

  /**
   * 과정에 등록되지 않은 교육생 목록을 조회
   * @param pageUserReqDTO 페이징 및 검색 조건을 담은 요청 DTO
   * @param includeAll 전체 조회 여부
   *                   ( null: 수강기록이 없는 신입생만 조회,
   *                     true: 교육진행중인 교육생을 제외한 수료자/중퇴자 포함 )
   * @return 미등록 교육생 정보 리스트
   */
  @GetMapping("/api/learners/not-enrolled")
  @ResponseBody
  public List<UserRespDTO> getNotEnrolledLearners(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam(value = "includeAll", required = false) Boolean includeAll
  ) {
    // 과정에 배정되지 않은 교육생의 UserRespDTO를 담은 리스트를 반환
    return courseManagementService.findNotEnrolledLearnersAll(pageUserReqDTO, includeAll);
  }

  /**
   * 교육생을 과정에 배정 (insert)
   * @param payload 등록에 필요한 정보를 담은 요청 본문 (loginUserId, loginUserType, learnerId, courseId)
   * @return 등록 성공/실패 여부를 담은 ApiResponse의 ResponseEntity
   */
  @PostMapping("/api/learner-enrollments")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addLearnerToCourse(
      @RequestBody Map<String, Object> payload
  ) {

    // 요청 정보로 교육생 등록 시도
    boolean isSuccess =
        courseManagementService.addLearnerToCourse(
            (Integer) payload.get("loginUserId"),
            (String) payload.get("loginUserType"),
            (Integer) payload.get("learnerId"),
            (Integer) payload.get("courseId")
        );

    // 등록 성공 여부에 따라 응답 반환
    if (isSuccess) {
      return ApiResponse.okResponse(200, "등록 성공", null);
    } else {
      return ApiResponse.failResponse(
          409, "등록된 인원을 초과할 수 없습니다.", null, HttpStatus.CONFLICT
      );
    }
  }


  /**
   * 과정에서 교육생을 삭제(배정 삭제)
   * @param loginUserId 요청자(로그인 유저)의 ID
   * @param loginUserType 요청자(로그인 유저)의 타입
   * @param learnerId 배정 삭제할 교육생의 ID
   * @param courseId 배정 삭제할 과정의 ID
   * @return 삭제 성공/실패 여부를 담은 ApiResponse의 ResponseEntity
   */
  @DeleteMapping("/api/learner-enrollments")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeLearnerFromCourse(
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("learnerId") Integer learnerId,
      @RequestParam("courseId") Integer courseId
  ) {

    // 요청 정보로 교육생 배정 삭제 시도
    boolean isSuccess = courseManagementService.removeLearnerFromCourse(
        loginUserId, loginUserType, learnerId, courseId);

    // 삭제 성공 여부에 따라 응답 반환
    if (isSuccess) {
      return ApiResponse.okResponse(200, "삭제 성공", null);
    } else {
      return ApiResponse.failResponse(409, "삭제 실패", null, HttpStatus.CONFLICT);
    }
  }


  /**
   * 로그인 유저의 포지션 정보를 반영하여 미처리건의 개수를 조회
   * @param baseReqDTO 로그인 유저 정보 및 기타 기본 요청 정보를 담은 DTO
   * @param pageCourseRequest 과정 및 페이징 관련 요청 정보를 담은 DTO
   * @return 미완료 과제 개수(Map<String, Integer>)를 담은 ApiResponse의 ResponseEntity
   */
  @GetMapping("/api/coursemanagement/incompletetaskcount")
  @ResponseBody
  public ResponseEntity<ApiResponse<Map<String, Integer>>> getIncompleteTaskCount(
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageCourseRequest pageCourseRequest
  ) {
    // 로그인 유저의 포지션 정보 조회 및 세팅
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    // 미처리건의 개수를 조회
    Map<String, Integer> incompleteTaskCount =
        courseManagementService.getIncompleteTaskCount(
            baseReqDTO, pageCourseRequest);
    // 조회 결과(Map<String, Integer>)를 ApiResponse로 감싸서 반환
    return ApiResponse.okResponse(200, "success", incompleteTaskCount);
  }

  @GetMapping("/api/coursemanagement/courses")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseResponse<CourseOverviewResp>>>
  getCoursesByAuth(
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageCourseRequest pageCourseRequest,
      HttpServletRequest request
  ) {
    String referer = request.getHeader("Referer");
    log.info("■■■■■ referer: {}", referer);
    // 로그인유저 포지션 요청 후 SET
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    log.info("pageCourseRequest: {}", pageCourseRequest);
    PageCourseResponse<CourseOverviewResp> coursesWithPagination =
        courseManagementService.getCoursesByAuth(baseReqDTO, pageCourseRequest, request);
    log.info("coursesWithPagination: {}", coursesWithPagination);
    return ApiResponse.okResponse(200, "success", coursesWithPagination);
  }




  @DeleteMapping("/api/coursemanagement/courses")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeCoursesByAuth(
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageCourseRequest pageCourseRequest
  ) {
    // 로그인유저 포지션 요청 후 SET
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    Boolean isSuccess = courseManagementService.removeCoursesByAuth(baseReqDTO, pageCourseRequest);
    if (isSuccess) {
      return ApiResponse.okResponse(
          200, "삭제 성공", null);
    } else {
      return ApiResponse.failResponse(
          409,
          "잘못된 요청입니다. 다시 시도해주세요.",
          null, HttpStatus.CONFLICT);
    }

  }


}
