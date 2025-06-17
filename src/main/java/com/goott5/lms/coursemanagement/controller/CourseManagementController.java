package com.goott5.lms.coursemanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import com.goott5.lms.user.domain.UserVO;
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

  private final CourseManagementService courseManagementService;

  /**
   * 전체 과정 리스트 조회 API
   *
   * @param pageCourseReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param isInProgress
   * @return
   */
  @GetMapping("api/courses/all")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseRespDTO<CourseRespDTO>>> getCoursesAll(
      @ModelAttribute PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO,
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam(value = "isInProgress", required = false) Boolean isInProgress
  ) {

    PageCourseRespDTO<CourseRespDTO> courses =
        courseManagementService.findCoursesAll(
            pageCourseReqDTO, loginUserId, loginUserType, isInProgress);

    return ApiResponse.okResponse(200, "success", courses);
  }

  /**
   * 과정 상세 조회 API
   *
   * @param loginUserId
   * @param loginUserType
   * @param courseId
   * @return
   */
  @GetMapping("api/course")
  @ResponseBody
  public ResponseEntity<ApiResponse<CourseRespDTO>> getCourse(
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("courseId") Integer courseId
  ) {

    CourseRespDTO course =
        courseManagementService.findCourse(
            loginUserId, loginUserType, courseId);

    return ApiResponse.okResponse(200, "success", course);
  }

  /**
   * 교육생 배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param courseId
   * @return
   */
  @GetMapping("api/learners/enrolled")
  @ResponseBody
  public List<UserRespDTO> getEnrolledLearnersByCourseId(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam("courseId") Integer courseId
  ) {

    return courseManagementService.findEnrolledLearnersByCourseId(pageUserReqDTO, courseId);
  }

  /**
   * 교육생 미배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param includeAll
   * @return
   */
  @GetMapping("api/learners/not-enrolled")
  @ResponseBody
  public List<UserRespDTO> getNotEnrolledLearners(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam(value = "includeAll", required = false) Boolean includeAll
  ) {

    return courseManagementService.findNotEnrolledLearnersAll(pageUserReqDTO, includeAll);
  }

  /**
   * 교육생 배정 '추가' API
   *
   * @param payload
   * @return
   */
  @PostMapping("api/learner-enrollments")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addLearnerToCourse(
      @RequestBody Map<String, Object> payload
  ) {

    boolean isSuccess =
        courseManagementService.addLearnerToCourse(
            (Integer) payload.get("loginUserId"),
            (String) payload.get("loginUserType"),
            (Integer) payload.get("learnerId"),
            (Integer) payload.get("courseId")
        );

    if (isSuccess) {
      return ApiResponse.okResponse(200, "등록 성공", null);
    } else {
      return ApiResponse.failResponse(
          409, "등록된 인원을 초과할 수 없습니다.", null, HttpStatus.CONFLICT
      );
    }
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
  @DeleteMapping("api/learner-enrollments")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeLearnerFromCourse(
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("learnerId") Integer learnerId,
      @RequestParam("courseId") Integer courseId
  ) {

    boolean isSuccess = courseManagementService.removeLearnerFromCourse(
        loginUserId, loginUserType, learnerId, courseId);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "삭제 성공", null);
    } else {
      return ApiResponse.failResponse(409, "삭제 실패", null, HttpStatus.CONFLICT);
    }
  }

  /**
   * 과정 삭제 API
   *
   * @param commonReqDTO
   * @return
   */
  @DeleteMapping("api/course")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeCourse(
      @RequestBody CommonReqDTO commonReqDTO
  ) {

    Boolean isSuccess = courseManagementService.removeCourse(commonReqDTO);
    if (isSuccess) {
      return ApiResponse.okResponse(
          200, "삭제 성공", null);
    } else {
      return ApiResponse.failResponse(
          409,
          "과정시작일 전까지만 삭제 가능합니다.",
          null,
          HttpStatus.CONFLICT);
    }
  }

  /**
   * courseList 페이지
   *
   * @return
   */
  @GetMapping("courseManagement/courseList")
  public String courseList() {
    return "courseManagement/courseList";
  }

  /**
   * courseDetail 페이지
   *
   * @param courseId
   * @param model
   * @param session
   * @return
   */
  @GetMapping("courseManagement/courseDetail")
  public String courseDetail(
      @RequestParam(value = "courseId", defaultValue = "-1") Integer courseId,
      Model model,
      HttpSession session
  ) {

    if (courseId == -1) {
      return "courseManagement/courseList";
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();
    CourseRespDTO course =
        courseManagementService.findCourse(loginUserId, loginUserType, courseId);
    model.addAttribute("course", course);
    return "courseManagement/courseDetail";
  }

  /**
   * courseModify 페이지
   *
   * @param courseId
   * @param model
   * @param session
   * @return
   */
  @GetMapping("courseManagement/courseModify")
  public String courseModify(
      @RequestParam(value = "courseId", defaultValue = "-1") Integer courseId,
      Model model,
      HttpSession session
  ) {

    if (courseId == -1) { // 추후수정
      return "courseManagement/courseList";
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();
    CourseRespDTO course =
        courseManagementService.findCourse(loginUserId, loginUserType, courseId);
    model.addAttribute("course", course);
    return "courseManagement/courseModify";
  }

  /**
   * learnerAssignment 페이지
   *
   * @param session
   * @param model
   * @return
   */
  @GetMapping("courseManagement/learnerAssignment")
  public String learnerAssignment(HttpSession session, Model model) {

    model.addAttribute("loginUser", session.getAttribute("loginUser"));

    return "courseManagement/learnerAssignment";
  }


}
