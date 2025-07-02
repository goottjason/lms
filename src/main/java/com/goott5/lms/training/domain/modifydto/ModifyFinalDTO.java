package com.goott5.lms.training.domain.modifydto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ModifyFinalDTO {

  private int trainingId;
  private Map<String, String> postMap;

}
