package com.goott5.lms.training.domain.registerdto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InsertFinalRegisterDTO {

  private SelectCourseDTO selectCourseDTO;
  @Valid
  private List<RegisterTrainingParamDTO> dataArray;
  private String postDate;

}
