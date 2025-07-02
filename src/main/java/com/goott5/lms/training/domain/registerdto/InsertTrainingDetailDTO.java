package com.goott5.lms.training.domain.registerdto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InsertTrainingDetailDTO {

  int trainingId;
  int period;
  int plan;
  String actual;

}
