package com.goott5.lms.training.domain.registerdto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegisterTrainingParamDTO {

  private int period;
  private int plan;
  private String actual;

}
