package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import com.goott5.lms.test.domain.test.submission.TestSubmissionDTO;
import com.goott5.lms.test.service.test.detail.TestDetailService;
import com.goott5.lms.user.domain.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "시험 상세", description = "시험 수정·삭제 API")
@RequiredArgsConstructor
@Slf4j
public class TestDetailController {

  private final TestDetailService testDetailService;

  @GetMapping("/tests/{testId}")
  @Operation(summary = "시험 상세 조회", description = "주어진 ID의 시험 정보를 조회합니다.", parameters = {
      @Parameter(in = ParameterIn.PATH, name = "testId", description = "조회할 시험 ID", required = true, example = "1")
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestRegisterDTO.class)))
  })
  public ResponseEntity<ApiResult<TestRegisterDTO>> getTestDetail(
      @PathVariable("testId") int testId, HttpSession session) {

    TestRegisterDTO testDetail = testDetailService.getTestDetail(testId);

    return ApiResult.respondOk(200, ((UserVO) session.getAttribute("loginUser")).getType(),
        testDetail);
  }

  @GetMapping("/tests/{testId}/learners")
  @Operation(summary = "시험 응시자 조회", description = "주어진 시험 ID에 해당하는 수강생(응시자) 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LearnerInfoVO.class))))
  })
  public ResponseEntity<ApiResult<List<LearnerInfoVO>>> getTestLearners(
      @PathVariable("testId") int testId) {

    return ApiResult.respondOk(200, "SUCCESS", testDetailService.getTestLearners(testId));
  }

  @PutMapping("/tests/{testId}")
  @Operation(summary = "시험 정보 수정", description = "주어진 ID의 시험 상세 정보를 수정합니다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 시험 정보(JSON)", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestRegisterDTO.class)))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "400", description = "유효성 검사 오류", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
  })
  public <T> ResponseEntity<ApiResult<T>> modifyTestDetail(@PathVariable("testId") int testId,
      @Valid @RequestBody TestRegisterDTO testDetailDTO, BindingResult bindingResult) {

    log.info("testDetailDTO: {}", testDetailDTO);

    if (bindingResult.hasErrors()) {
      Map<String, String> errorsMap = new HashMap<>();

      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errorsMap.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
      return ApiResult.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    testDetailService.modifyTestDetail(testDetailDTO, testId);

    return ApiResult.<T>respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

  @DeleteMapping("/tests/{testId}")
  @Operation(summary = "시험 삭제", description = "주어진 ID의 시험을 삭제합니다.", parameters = {
      @Parameter(in = ParameterIn.PATH, name = "testId", description = "삭제할 시험 ID", required = true, example = "1")
  })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
  })
  public ResponseEntity<ApiResult<String>> removeTestDetail(@PathVariable("testId") int testId) {
    testDetailService.removeTestDetail(testId);
    return ApiResult.respondOk(200, "SUCCESS", "SUCCESS");
  }
}
