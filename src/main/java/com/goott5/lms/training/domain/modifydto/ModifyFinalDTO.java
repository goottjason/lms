package com.goott5.lms.training.domain.modifydto;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ModifyFinalDTO {

  private int trainingId;
  @Valid
  private Map<String, String> postMap; // trainingDetailId : actual(trainingDetail에 속하는)

}
