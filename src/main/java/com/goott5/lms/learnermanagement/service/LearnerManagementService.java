package com.goott5.lms.learnermanagement.service;

import com.goott5.lms.learnermanagement.domain.*;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;

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
}
