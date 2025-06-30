package com.goott5.lms.training.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectTrainingDetailDTO {

  private Integer id;
  private Integer trainingId;
  private Integer period;
  private Integer plan;
  private String actual;

}
