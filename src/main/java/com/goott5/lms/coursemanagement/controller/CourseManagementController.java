package com.goott5.lms.coursemanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseGetReqDTO;
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
  private final LearnerManagementService learnerManagementService;

  @GetMapping("api/management/courses")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseRespDTO<CourseRespDTO>>> getCoursesAllorOne(
      @ModelAttribute CommonReqDTO commonReqDTO,
      @ModelAttribute PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO
  ) {
    log.info("commonReqDTO: {}", commonReqDTO);
    log.info("pageCourseReqDTO: {}", pageCourseReqDTO);
    PageCourseRespDTO<CourseRespDTO> courses =
        courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

    return ApiResponse.okResponse(200, "success", courses);
  }


  /*@GetMapping("api/course")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseRespDTO<CourseRespDTO>>> getCourse(
      @ModelAttribute CourseGetReqDTO courseGetReqDTO
  ) {

    PageCourseRespDTO<CourseRespDTO> course =
        courseManagementService.findCoursesAllorOne(courseGetReqDTO);

    return ApiResponse.okResponse(200, "success", course);
  }*/


  @GetMapping("api/learners/enrolled")
  @ResponseBody
  public List<UserRespDTO> getEnrolledLearnersByCourseId(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam("courseId") Integer courseId
  ) {

    return courseManagementService.findEnrolledLearnersByCourseId(pageUserReqDTO, courseId);
  }


  @GetMapping("api/learners/not-enrolled")
  @ResponseBody
  public List<UserRespDTO> getNotEnrolledLearners(
      @ModelAttribute PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @RequestParam(value = "includeAll", required = false) Boolean includeAll
  ) {

    return courseManagementService.findNotEnrolledLearnersAll(pageUserReqDTO, includeAll);
  }


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


  @GetMapping("courseManagement/courseList")
  public String courseList() {
    return "courseManagement/courseList";
  }


  @GetMapping("courseManagement/courseDetail")
  public String courseDetail(
      @RequestParam(value = "courseId", defaultValue = "-1") Integer courseId,
      Model model,
      HttpSession session
  ) {

    if (courseId == -1) {
      /// ///////////////////////////////////////////////////////
      UserVO loginUser = (UserVO) session.getAttribute("loginUser");
      Integer loginUserId = Integer.valueOf(loginUser.getId());
      String loginUserType = loginUser.getType();
      CommonReqDTO commonReqDTO = CommonReqDTO.builder()
          .loginUserId(loginUserId)
          .loginUserType(loginUserType)
          .courseId(null)
          .isInProgress(null)
          .build();
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = PageCourseReqDTO.<CourseReqDTO>builder()
          .orderBy("is_in_progress")
          .orderDirection("DESC")
          .build();
      PageCourseRespDTO<CourseRespDTO> courses =
          courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

      model.addAttribute("course", courses.getRespDTOS().get(0));
      return "courseManagement/courseDetail";
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();
    CommonReqDTO commonReqDTO = CommonReqDTO.builder()
        .loginUserId(loginUserId)
        .loginUserType(loginUserType)
        .courseId(courseId)
        .isInProgress(null)
        .build();
    PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = new PageCourseReqDTO<CourseReqDTO>();
    PageCourseRespDTO<CourseRespDTO> courses =
        courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

    model.addAttribute("course", courses.getRespDTOS().get(0));
    return "courseManagement/courseDetail";
  }


  /*@GetMapping("courseManagement/courseModify")
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
    CommonReqDTO commonReqDTO = CommonReqDTO.builder()
        .loginUserId(loginUserId)
        .loginUserType(loginUserType)
        .courseId(courseId)
        .isInProgress(null)
        .build();
    PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = new PageCourseReqDTO<CourseReqDTO>();
    PageCourseRespDTO<CourseRespDTO> courses =
        courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

    model.addAttribute("course", courses.getRespDTOS().get(0));
    return "courseManagement/courseModify";
  }*/


  @GetMapping("courseManagement/learnerAssignment")
  public String learnerAssignment(HttpSession session, Model model) {

    model.addAttribute("loginUser", session.getAttribute("loginUser"));

    return "courseManagement/learnerAssignment";
  }


  @GetMapping("/api/coursemanagement/courses")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageCourseResponse<CourseOverviewResp>>>
  getCoursesByAuth(
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageCourseRequest pageCourseRequest
  ) {
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    PageCourseResponse<CourseOverviewResp> coursesWithPagination =
        courseManagementService.getCoursesByAuth(baseReqDTO, pageCourseRequest);
    return ApiResponse.okResponse(200, "success", coursesWithPagination);
  }

}
