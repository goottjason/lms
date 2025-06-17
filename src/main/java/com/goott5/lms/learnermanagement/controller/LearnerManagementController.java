package com.goott5.lms.learnermanagement.controller;

import com.goott5.lms.coursemanagement.domain.ApiResponse;
import com.goott5.lms.learnermanagement.domain.*;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
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
import java.util.List;
import java.util.Map;

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
  @GetMapping("learnerManagement/learnerList")
  public String learnerList() {
    return "/learnerManagement/learnerList";
  }

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
  @GetMapping("api/learners/all")
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


  /**
   * 특정 과정에 수강한 교육생 출결 조회 API
   *
   * @param pageParticipationReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param leId
   * @return
   */
  @GetMapping("api/participations")
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
   * learnerDetail 페이지
   *
   * @param leId
   * @param isInProgress
   * @param courseId
   * @param model
   * @param session
   * @return
   */
  @GetMapping("learnerManagement/learnerDetail")
  public String learnerDetail(
      @RequestParam(value = "leId", defaultValue = "-1") Integer leId,
      @RequestParam(value = "isInProgress", required = false) Boolean isInProgress,
      @RequestParam(value = "courseId", required = false) Integer courseId,
      Model model,
      HttpSession session
  ) {
    if (leId == -1) {
      return "/learnerManagement/learnerList";
    }
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = Integer.valueOf(loginUser.getId());
    String loginUserType = loginUser.getType();
    PageLearnerReqDTO<LearnerReqDTO> pageLearnerReqDTO = new PageLearnerReqDTO();
    PageLearnerRespDTO<LearnerRespDTO> personal =
        learnerManagementService.findLearnersAll(
            pageLearnerReqDTO, loginUserId, loginUserType, leId, isInProgress, courseId);
    log.info("personal: " + personal);
    HashMap<String, Integer> statusCountMap = personal.getRespDTOS().get(0)
        .getPageParticipationRespDTO().getStatusCountMap();
    log.info("statusCountMap: " + statusCountMap);
    model.addAttribute("personal", personal.getRespDTOS().get(0));
    model.addAttribute("statusCountMap", statusCountMap);
    // model.addAttribute("participations",);
    return "/learnerManagement/learnerDetail";
  }

  /**
   * 특정 과정에 수강한 교육생의 취업관리 데이터 '수정' API
   *
   * @param request
   * @return
   */
  @PostMapping("api/learner-employment-support")
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
}
