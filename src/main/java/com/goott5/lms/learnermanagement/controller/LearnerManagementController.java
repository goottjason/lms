package com.goott5.lms.learnermanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.learnermanagement.domain.*;
import com.goott5.lms.learnermanagement.domain.dto.CompletionStatusUpdateRequest;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerResponse;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.table.ParticipationWithReason;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LearnerManagementController {

  private final LearnerManagementService learnerManagementService;

  /**
   * learnerList 페이지
   *
   * @return
   */


  /**
   * 전체 교육생 수강 조회 API
   *
   * @param pageLearnerReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param courseId
   * @param isInProgress
   * @return
   */
  @GetMapping("/api/learners/all")
  @ResponseBody // value = "courseId", required = false
  public PageLearnerRespDTO<LearnerRespDTO> getLearnersAll(
      @ModelAttribute PageLearnerReqDTO<LearnerReqDTO> pageLearnerReqDTO,
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam(value = "courseId", required = false) Integer courseId,
      @RequestParam(value = "isInProgress", required = false) Boolean isInProgress) {
    Integer leId = null;
    PageLearnerRespDTO<LearnerRespDTO> learners =
        learnerManagementService.findLearnersAll(
            pageLearnerReqDTO, loginUserId, loginUserType, leId, isInProgress, courseId);

    log.info("learners: " + learners);

    return learners;
  }

  @GetMapping("/api/management/learners")
  public ResponseEntity<ApiResponse<PageStaffRespDTO<StaffRespDTO>>> getLearnersAllorOne (
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageLearnerReqDTO<LearnerReqDTO> pageLernerReqDTO
  ) {
    PageLearnerRespDTO<LearnerRespDTO> learners =
        learnerManagementService.getLearnersAllorOne(baseReqDTO, pageLernerReqDTO);
    log.info("■■■learners: " + learners);
    return null;
  }


  /**
   * 특정 과정에 수강한 교육생 출결 조회 API
   *
   * @param pageParticipationReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param leId
   * @return
   */
  @GetMapping("/api/participations")
  @ResponseBody
  public PageParticipationRespDTO<ParticipationRespDTO> getParticipations(
      @ModelAttribute PageParticipationReqDTO<ParticipationReqDTO> pageParticipationReqDTO,
      @RequestParam("loginUserId") Integer loginUserId,
      @RequestParam("loginUserType") String loginUserType,
      @RequestParam("leId") Integer leId
  ) {
    log.info("pageParticipationReqDTO: " + pageParticipationReqDTO);
    PageParticipationRespDTO<ParticipationRespDTO> participationsWithPagination =
        learnerManagementService.findParticipations(pageParticipationReqDTO, loginUserId,
            loginUserType, leId);
    log.info("★participationsWithPagination: " + participationsWithPagination);
    return participationsWithPagination;
  }



  /**
   * 특정 과정에 수강한 교육생의 취업관리 데이터 '수정' API
   *
   * @param request
   * @return
   */
  @PostMapping("/api/learner-employment-support")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> updateEmploymentSupport(
      @RequestBody EmploymentSupportUpdateReqDTO request) {

    Boolean isSuccess = learnerManagementService.updateEmploymentSupport(
        request.getLoginUserId(),
        request.getLoginUserType(),
        request.getLeId(),
        request.getReqDTO()
    );

    if (isSuccess) {
      return ApiResponse.okResponse(200, "등록 성공", null);
    } else {
      return ApiResponse.failResponse(
          409, "수정할 수 없습니다.", null, HttpStatus.CONFLICT
      );
    }
  }

  @GetMapping("/learnerManagement/participationTemp")
  public String participationTemp() {

      return "learnerManagement/participationTemp";
  }
  @GetMapping("/learnerManagement/participationList")
  public String participationList() {

    return "learnerManagement/participationList";
  }
  @GetMapping("/learnerManagement/participationList2")
  public String participationList2() {

    return "learnerManagement/participationList2";
  }





  @GetMapping("/api/management/participation/{pid}")
  @ResponseBody
  public ResponseEntity<ApiResponse<ParticipationWithReason>> getPartInfoByPid(
      @PathVariable Integer pid
  ) {
    ParticipationWithReason partInfo = learnerManagementService.getPartInfoByPid(pid);
    log.info("partInfo: " + partInfo);
    return ApiResponse.okResponse(200, "success", partInfo);
  }

























// =============================================================================
  @GetMapping("/learnerManagement/learnerList")
  public String learnerList() {
    return "learnerManagement/learnerList";
  }

  @GetMapping("/api/learnermanagement/learners")
  @ResponseBody
  public ResponseEntity<ApiResponse<PageLearnerResponse<LearnerOverviewResp>>> getLearnersByAuth(
      @ModelAttribute BaseReqDTO baseReqDTO,
      @ModelAttribute PageLearnerRequest pageLearnerRequest
  ) {
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    PageLearnerResponse<LearnerOverviewResp> learnersWithPagination =
        learnerManagementService.getLearnersByAuth(baseReqDTO, pageLearnerRequest);
    return ApiResponse.okResponse(200, "success", learnersWithPagination);
  }

  @GetMapping("/learnerManagement/learnerDetail")
  public String learnerDetail(
      @RequestParam(value = "leId", defaultValue = "-1") Integer leId,
      Model model,
      HttpSession session
  ) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();

    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(loginUserId)
        .loginUserType(loginUserType)
        .build();

    // 로그인유저 포지션 요청 후 SET
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(baseReqDTO.getLoginUserId());
    baseReqDTO.setLoginUserPosition(loginUserPosition);

    PageLearnerRequest pageLearnerRequest = PageLearnerRequest.builder()
        .pageNo(null)
        .pageSize(null)
        .type("userFullname")
        .keyword(null)
        .orderBy("userFullname")
        .orderDirection("ASC")
        .coIsInProgress(null)
        .leCourseId(null)
        .leId(leId)
        .build();

    // 쿼리스트링 없이 요청 (잘못된 요청)
    if (leId == -1) {
      return "learnerManagement/learnerList";
    }

    PageLearnerResponse<LearnerOverviewResp> learnersWithPagination =
        learnerManagementService.getLearnersByAuth(baseReqDTO, pageLearnerRequest);

    model.addAttribute("record", learnersWithPagination.getRecords().get(0));

    return "learnerManagement/learnerDetail";
  }

  @PatchMapping("/api/learnermanagement/learners/enrollments/{leId}")
  @ResponseBody
  public ResponseEntity<ApiResponse<Boolean>> modifyCompletionStatus(
      @PathVariable Integer leId,
      @RequestBody CompletionStatusUpdateRequest request
  ) {
    String loginUserPosition =
        learnerManagementService.getLoginUserPositionByUserId(request.getLoginUserId());
    request.setLoginUserPosition(loginUserPosition);
    request.setLeId(leId);

    Boolean result = false;

    if (request.getLoginUserType().equals("ADMINISTRATOR")) {
       result = learnerManagementService.modifyCompletionStatus(request);
    }

    if (result) {
      return ApiResponse.okResponse(200, "success", result);
    } else {
      return ApiResponse.okResponse(200, "fail", result);
    }
  }
}
