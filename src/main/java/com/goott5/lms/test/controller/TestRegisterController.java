package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.test.domain.test.list.TestListDTO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.service.test.register.TestRegisterService;
import com.goott5.lms.user.domain.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "시험 등록", description = "시험 조회·등록 API")
@RequiredArgsConstructor
@Slf4j
public class TestRegisterController {

  private final TestRegisterService testRegisterService;

  @GetMapping("/tests")
  @Operation(summary = "시험 리스트 조회", description = "로그인 후 발급된 JSESSIONID 쿠키가 있어야 호출 가능합니다.", parameters = {
      @Parameter(in = ParameterIn.QUERY, name = "courseName", description = "검색할 강좌명 (생략 시 전체)", schema = @Schema(type = "string")),
      @Parameter(in = ParameterIn.QUERY, name = "currentPageNo", description = "페이지 번호 (기본 1)", schema = @Schema(type = "int", defaultValue = "1")),
      @Parameter(in = ParameterIn.COOKIE, name = "JSESSIONID", description = "로그인 세션 식별용 쿠키", required = true, schema = @Schema(type = "string"))
  })

  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseVO.class)))
  })
  public ResponseEntity<ApiResult<ResponseVO<TestListDTO>>> getTestLst(
      @RequestParam(required = false) String courseName,
      @RequestParam(defaultValue = "1") int currentPageNo,
      @Parameter(hidden = true) HttpSession session) {
    log.info("courseName: {}", courseName);
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if (currentPageNo == 0) {
      currentPageNo = 1;
    }

    ResponseVO<TestListDTO> responseVO = testRegisterService.getTestList(courseName,
        currentPageNo);

    return ApiResult.respondOk(200, loginUser.getType(), responseVO);
  }

  @PostMapping("/tests")
  @Operation(summary = "시험 등록", description = "새 시험을 등록하는 메서드 입니다. 로그인 후 발급된 JSESSIONID 쿠키가있어야 호출 가능합니다.", parameters = {
      @Parameter(in = ParameterIn.COOKIE, name = "JSESSIONID", description = "로그인 세션 식별용 쿠키", required = true, schema = @Schema(type = "string"))
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "등록할 시험 정보(JSON)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestRegisterDTO.class))))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "400", description = "유효성 검사 오류", content = @Content(mediaType = "application/json"))
  })
  public <T> ResponseEntity<ApiResult<T>> createTest(
      @Valid @RequestBody TestRegisterDTO testRegisterDTO,
      BindingResult bindingResult, @Parameter(hidden = true) HttpSession session) {

    if (bindingResult.hasErrors()) {

      Map<String, String> errorsMap = new HashMap<>();
      for (FieldError err : bindingResult.getFieldErrors()) {
        errorsMap.put(err.getField(), err.getDefaultMessage());
      }

      log.info("errors={}", errorsMap);

      return ApiResult.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    testRegisterService.createTest(testRegisterDTO, session);

    return ApiResult.respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

  @GetMapping("/test/courseId")
  public ResponseEntity<ApiResult<Integer>> getCourseId(
      @RequestParam(name = "courseName", required = true) String courseName
  ) {

    return ApiResult.respondOk(200, "SUCCESS", testRegisterService.getCourseId(courseName));
  }

}
