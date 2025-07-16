package com.goott5.lms.operationsmanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.learnermanagement.domain.LearnerReqDTO;
import com.goott5.lms.learnermanagement.domain.LearnerRespDTO;
import com.goott5.lms.learnermanagement.domain.PageLearnerReqDTO;
import com.goott5.lms.learnermanagement.domain.PageLearnerRespDTO;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerResponse;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomUsageResp;
import com.goott5.lms.operationsmanagement.domain.IntegratedReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffHistoryReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffHistoryRespDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.StaffHistoryReq;
import com.goott5.lms.operationsmanagement.domain.StaffHistoryResp;
import com.goott5.lms.operationsmanagement.domain.StaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.dto.PageStaffRequest;
import com.goott5.lms.operationsmanagement.domain.dto.PageStaffResponse;
import com.goott5.lms.operationsmanagement.domain.dto.StaffResponse;
import com.goott5.lms.operationsmanagement.domain.integrated.StaffOverviewResp;
import com.goott5.lms.operationsmanagement.service.OperationsManagementService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OperationsManagementController {

  // ========== 서비스 주입
  private final OperationsManagementService operationsManagementService;
  private final LearnerManagementService learnerManagementService;

  // ========== 페이지 렌더링

  /**
   * 교직원 목록 페이지 반환
   *
   * @return 교직원 목록 페이지 뷰 이름
   */
  @GetMapping("/operationsManagement/staffList")
  public String staffList() { return "operationsManagement/staffList"; }

  /**
   * 교직원 상세 페이지를 반환
   *
   * @param staffId 교직원 ID
   * @param model Model 객체
   * @param session HttpSession 객체
   * @return 교직원 상세 페이지 뷰 이름
   */
  @GetMapping("/operationsManagement/staffDetail")
  public String staffDetail(
      @RequestParam(value = "staffId", defaultValue = "-1") Integer staffId,
      Model model,
      HttpSession session
  ) {
    if (staffId == -1) {
      return "operationsManagement/staffList";
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();

    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(loginUserId)
        .loginUserType(loginUserType)
        .build();
    PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO = PageStaffReqDTO.<StaffReqDTO>builder()
        .staffId(staffId)
        .includeLeaveDate(true)
        .build();

    PageStaffRespDTO<StaffRespDTO> staffs =
        operationsManagementService.getStaffsAllorOne(baseReqDTO, pageStaffReqDTO);


    model.addAttribute("staffs", staffs.getStaffRepsDTOS().get(0));
    return "operationsManagement/staffDetail";
  }

  /**
   * 강의실 목록 페이지를 반환합니다.
   *
   * @return 강의실 목록 페이지 뷰 이름
   */
  @GetMapping("/operationsManagement/classroomList")
  public String classroomList() { return "operationsManagement/classroomList"; }

  // ========== API

  /**
   * 교직원 이력 목록을 조회합니다.
   *
   * @param baseReqDTO 기본 요청 DTO
   * @param pageStaffHistoryReqDTO 교직원 이력 페이징 요청 DTO
   * @return 교직원 이력 목록과 페이징 정보가 포함된 ApiResponse
   */
  @GetMapping("/api/management/staffhistories")
  public ResponseEntity<ApiResponse<PageStaffHistoryRespDTO<StaffHistoryResp>>> getStaffHistories (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageStaffHistoryReqDTO<StaffHistoryReq> pageStaffHistoryReqDTO
  ) {
    PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO = PageStaffReqDTO.<StaffReqDTO>builder()
        .staffId(pageStaffHistoryReqDTO.getStaffId())
        .includeLeaveDate(true)
        .build();

    PageStaffRespDTO<StaffRespDTO> staffs =
        operationsManagementService.getStaffsAllorOne(baseReqDTO, pageStaffReqDTO);

    List<StaffHistoryReq> historiesReq = new ArrayList<>();
    if(staffs.getStaffRepsDTOS().get(0).getAssignmentHistoryList() != null) {
      historiesReq = staffs.getStaffRepsDTOS().get(0)
          .getAssignmentHistoryList();
    }

    PageStaffHistoryRespDTO pageStaffHistoryRespDTO = PageStaffHistoryRespDTO.withPageInfo()
        .staffHistories(historiesReq)
        .pageStaffHistoryReqDTO(pageStaffHistoryReqDTO)
        .build();
    return ApiResponse.okResponse(200, "success", pageStaffHistoryRespDTO);
  }

  /**
   * 교직원의 퇴사일자를 업데이트합니다.
   *
   * @param loginUserId 로그인한 사용자 ID
   * @param loginUserType 로그인한 사용자 유형
   * @param staffId 교직원 ID
   * @param leaveDate 퇴사일자(yyyy-MM-dd)
   * @return 처리 결과 ApiResponse
   */
  @PatchMapping("/api/management/staff")
  public ResponseEntity<ApiResponse<StaffRespDTO>> updateLeaveDateByStaffId (
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("staffId") Integer staffId,
      @RequestParam("leaveDate") String leaveDate
  ) {

    Boolean isSuccess = operationsManagementService.updateLeaveDateByStaffId(loginUserId, loginUserType, staffId, leaveDate);
    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  /**
   * 교직원 목록(또는 단일 교직원)을 조회합니다.
   *
   * @param baseReqDTO 기본 요청 DTO
   * @param pageStaffRequest 교직원 페이징 요청 DTO
   * @return 교직원 목록과 페이징 정보가 포함된 ApiResponse
   */
  @GetMapping("/api/operationsmanagement/staffs")
  public ResponseEntity<ApiResponse<PageStaffResponse<StaffResponse>>> getStaffsAllorOne (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageStaffRequest pageStaffRequest
  ) {

    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    PageStaffResponse<StaffResponse> staffsWithPagination =
        operationsManagementService.getStaffsByAuth(baseReqDTO, pageStaffRequest);
    return ApiResponse.okResponse(200, "success", staffsWithPagination);
  }

  /**
   * 강의실 목록(또는 단일 강의실)을 조회합니다.
   *
   * @param baseReqDTO 기본 요청 DTO
   * @param pageClassroomReqDTO 강의실 페이징 요청 DTO
   * @return 강의실 목록과 페이징 정보가 포함된 ApiResponse
   */
  @GetMapping("/api/management/classrooms")
  public ResponseEntity<ApiResponse<PageClassroomRespDTO<ClassroomRespDTO>>> getClassroomsAllorOne (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageClassroomReqDTO<ClassroomReqDTO> pageClassroomReqDTO
  ) {

    PageClassroomRespDTO<ClassroomRespDTO> classrooms =
        operationsManagementService.getClassroomsAllorOne(baseReqDTO, pageClassroomReqDTO);

    return ApiResponse.okResponse(200, "success", classrooms);
  }

  /**
   * 강의실 정보를 수정합니다.
   *
   * @param integratedReqDTO 통합 요청 DTO
   * @return 처리 결과 ApiResponse
   */
  @PatchMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> updateClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {

    Boolean isSuccess = operationsManagementService.updateClassroom(integratedReqDTO);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  /**
   * 강의실 정보를 삭제합니다.
   *
   * @param integratedReqDTO 통합 요청 DTO
   * @return 처리 결과 ApiResponse
   */
  @DeleteMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> deleteClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {

    Boolean isSuccess = operationsManagementService.deleteClassroom(integratedReqDTO);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  /**
   * 강의실 정보를 추가합니다.
   *
   * @param integratedReqDTO 통합 요청 DTO
   * @return 처리 결과 ApiResponse
   */
  @PostMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {

    Boolean isSuccess = operationsManagementService.addClassroom(integratedReqDTO);
    log.info("isSuccess: {}", isSuccess);
    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  /**
   * 강의실 사용 현황을 조회합니다.
   *
   * @return 강의실 사용 현황 리스트가 포함된 ApiResponse
   */
  @GetMapping("/api/operationmanagement/classroom/usage")
  public ResponseEntity<ApiResponse<List<ClassroomUsageResp>>> getClassroomUsage () {

    List<ClassroomUsageResp> classroomUsage = operationsManagementService.getClassroomUsage();

    return ApiResponse.okResponse(200, "success", classroomUsage);
  }
}