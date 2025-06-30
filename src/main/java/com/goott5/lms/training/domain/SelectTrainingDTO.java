package com.goott5.lms.training.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectTrainingDTO {

  private Integer id;
  private Integer courseId;
  private Date trainingDate;
  private Integer instructorId;

}
