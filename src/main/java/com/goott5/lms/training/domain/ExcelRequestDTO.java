package com.goott5.lms.training.domain;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExcelRequestDTO {

  private SelectAllTrainingDTO finalSelectTraining;
  private Map<String, String> detailWithSubMap;

}
