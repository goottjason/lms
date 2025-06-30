package com.goott5.lms.training.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@NotNull
@Builder
@Data
public class SelectAllTrainingDTO {

  private SelectTrainingDTO selectTrainingDTO;
  private String name; //과정명
  private Date trainingDate; //훈련일자
  private int numberOfLearner; //총 수강생 수
  private ResponseParticipationDTO responseParticipationDTO; // 출결 현황
  private List<SelectTrainingDetailDTO> selectTrainingDetailDTOList; // 훈련일지 detail 상황


}
