package com.goott5.lms.operationsmanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.learnermanagement.domain.LearnerReqDTO;
import com.goott5.lms.learnermanagement.domain.LearnerRespDTO;
import com.goott5.lms.learnermanagement.domain.PageLearnerReqDTO;
import com.goott5.lms.learnermanagement.domain.PageLearnerRespDTO;
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
  private final OperationsManagementService operationsManagementService;

  @GetMapping("/operationsManagement/staffList")
  public String staffList() { return "operationsManagement/staffList"; }

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

    log.info("staffList: {}", staffs);

    model.addAttribute("staffs", staffs.getStaffRepsDTOS().get(0));
    return "operationsManagement/staffDetail";
  }


  @GetMapping("/api/management/staffhistories")
  public ResponseEntity<ApiResponse<PageStaffHistoryRespDTO<StaffHistoryResp>>> getStaffHistories (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageStaffHistoryReqDTO<StaffHistoryReq> pageStaffHistoryReqDTO
  ) {
    log.info("pageStaffHistoryReqDTO: {}", pageStaffHistoryReqDTO);
    PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO = PageStaffReqDTO.<StaffReqDTO>builder()
        .staffId(pageStaffHistoryReqDTO.getStaffId())
        .includeLeaveDate(true)
        .build();

    PageStaffRespDTO<StaffRespDTO> staffs =
        operationsManagementService.getStaffsAllorOne(baseReqDTO, pageStaffReqDTO);

    log.info("★★★★{}", staffs.getStaffRepsDTOS().get(0));
    List<StaffHistoryReq> historiesReq = new ArrayList<>();
    if(staffs.getStaffRepsDTOS().get(0).getAssignmentHistoryList() != null) {
      historiesReq = staffs.getStaffRepsDTOS().get(0)
          .getAssignmentHistoryList();
    }

    PageStaffHistoryRespDTO pageStaffHistoryRespDTO = PageStaffHistoryRespDTO.withPageInfo()
        .staffHistories(historiesReq)
        .pageStaffHistoryReqDTO(pageStaffHistoryReqDTO)
        .build();
    log.info(pageStaffHistoryRespDTO.toString());
    return ApiResponse.okResponse(200, "success", pageStaffHistoryRespDTO);
  }

  @PatchMapping("/api/management/staff")
  public ResponseEntity<ApiResponse<StaffRespDTO>> updateLeaveDateByStaffId (
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("staffId") Integer staffId,
      @RequestParam("leaveDate") String leaveDate
  ) {
    log.info("loginUserId: {}", loginUserId);
    log.info("loginUserType: {}", loginUserType);
    log.info("staffId: {}", staffId);
    log.info("leaveDate: {}", leaveDate);

    Boolean isSuccess = operationsManagementService.updateLeaveDateByStaffId(loginUserId, loginUserType, staffId, leaveDate);
    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }
  @GetMapping("/api/management/staffs")
  public ResponseEntity<ApiResponse<PageStaffRespDTO<StaffRespDTO>>> getStaffsAllorOne (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO
  ) {

    log.info("baseReqDTO:{}, pageStaffReqDTO: {}", baseReqDTO, pageStaffReqDTO);
    PageStaffRespDTO<StaffRespDTO> staffs =
        operationsManagementService.getStaffsAllorOne(baseReqDTO, pageStaffReqDTO);
    log.info("staffs:{}", staffs);

    return ApiResponse.okResponse(200, "success", staffs);
  }

  @GetMapping("/operationsManagement/classroomList")
  public String classroomList() { return "operationsManagement/classroomList"; }

  @GetMapping("/api/management/classrooms")
  public ResponseEntity<ApiResponse<PageClassroomRespDTO<ClassroomRespDTO>>> getClassroomsAllorOne (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageClassroomReqDTO<ClassroomReqDTO> pageClassroomReqDTO
  ) {

    log.info("baseReqDTO:{}, pageClassroomReqDTO: {}", baseReqDTO, pageClassroomReqDTO);
    PageClassroomRespDTO<ClassroomRespDTO> classrooms =
        operationsManagementService.getClassroomsAllorOne(baseReqDTO, pageClassroomReqDTO);

    return ApiResponse.okResponse(200, "success", classrooms);
  }

  @PatchMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> updateClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {
    log.info("updateClassroom");
    log.info("integratedReqDTO:{}", integratedReqDTO);

    Boolean isSuccess = operationsManagementService.updateClassroom(integratedReqDTO);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  @DeleteMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> deleteClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {
    log.info("updateClassroom");
    log.info("integratedReqDTO:{}", integratedReqDTO);

    Boolean isSuccess = operationsManagementService.deleteClassroom(integratedReqDTO);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  @PostMapping("/api/management/classroom")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addClassroom(
      @RequestBody IntegratedReqDTO integratedReqDTO
  ) {
    log.info("updateClassroom");
    log.info("integratedReqDTO:{}", integratedReqDTO);

    Boolean isSuccess = operationsManagementService.addClassroom(integratedReqDTO);

    if (isSuccess) {
      return ApiResponse.okResponse(200, "success", null);
    } else {
      return ApiResponse.failResponse(400, "fail", null, HttpStatus.GATEWAY_TIMEOUT);

    }
  }

  @GetMapping("/api/operationmanagement/classroom/usage")
  public ResponseEntity<ApiResponse<List<ClassroomUsageResp>>> getClassroomUsage () {

    List<ClassroomUsageResp> classroomUsage = operationsManagementService.getClassroomUsage();

    return ApiResponse.okResponse(200, "success", classroomUsage);
  }
}
