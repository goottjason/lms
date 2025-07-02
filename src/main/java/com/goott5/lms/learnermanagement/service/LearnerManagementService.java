package com.goott5.lms.learnermanagement.service;

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
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;

public interface LearnerManagementService {


  PageLearnerRespDTO<LearnerRespDTO> findLearnersAll(
      PageLearnerReqDTO<LearnerReqDTO> pageLearnerReqDTO, Integer loginUserId, String loginUserType,
      Integer leId, Boolean isInProgress,
      Integer courseId);

  PageParticipationRespDTO<ParticipationRespDTO> findParticipations(
      PageParticipationReqDTO<ParticipationReqDTO> pageParticipationReqDTO, Integer loginUserId,
      String loginUserType, Integer leId);

  Boolean updateEmploymentSupport(Integer loginUserId, String loginUserType, Integer leId,
      EmploymentSupportReqDTO employmentSupportReqDTO);

  PageLearnerRespDTO<LearnerRespDTO> getLearnersAllorOne(
      BaseReqDTO baseReqDTO,
      PageLearnerReqDTO<LearnerReqDTO> pageLernerReqDTO);

  PageLearnerResponse<LearnerOverviewResp> getLearnersByAuth(
      BaseReqDTO baseReqDTO,
      PageLearnerRequest pageLearnerRequest
  );

  String getLoginUserPositionByUserId(Integer loginUserId);

  ParticipationWithReason getPartInfoByPid(Integer pid);

  Boolean modifyCompletionStatus(CompletionStatusUpdateRequest request);

  Boolean modifyCompletionStatusByCoId(BaseReqDTO baseReqDTO, Integer coId);
}
