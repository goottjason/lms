package com.goott5.lms.test.controller.course;

import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.course.CourseInfoDTO;
import com.goott5.lms.test.service.course.TestCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "강좌", description = "강좌 조회·필터링 API")
@SecurityRequirement(name = "sessionAuth")
@RequiredArgsConstructor
public class TestCourseController {

  private final TestCourseService testCourseService;

  @GetMapping("/admin/courses")
  @Operation(summary = "관리자를 위한 강좌 API", description = "관리자의 진행 여부 조건에 맞는 강좌명을 조회하는 메서드입니다.", parameters = {
      @Parameter(in = ParameterIn.HEADER, name = "isInProgress", description = "강좌의 진행 여부 조건", required = false, example = "true")
  })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseInfoDTO.class)))
  })
  public ResponseEntity<ApiResult<List<String>>> getCourseListForAdmin(
      @RequestParam(required = false) Boolean isInProgress) {

    List<String> courseList = testCourseService.getCourseListForAdmin(isInProgress);
    return ApiResult.respondOk(200, "SUCCESS", courseList);
  }


  @GetMapping("/courses")
  @Operation(summary = "사용자(수강생|강사)를 위한 강좌 API", description = "로그인한 사용자에 맞는 강좌명을 조회하는 메서드입니다.", parameters = {
      @Parameter(in = ParameterIn.COOKIE, name = "JSESSIONID", description = "로그인 세션 식별용 쿠키", required = true)
  })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseInfoDTO.class)))
  })
  public ResponseEntity<ApiResult<List<CourseInfoDTO>>> getCourseListForUser(
      @Parameter(hidden = true) HttpSession session) {

    List<CourseInfoDTO> courseList = testCourseService.getCourseListForUser(session);

    return ApiResult.respondOk(200, "SUCCESS", courseList);
  }

}
