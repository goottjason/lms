package com.goott5.lms.training.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class SelectTrainingDTO {

  private Integer id;
  private Integer courseId;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date trainingDate;
  private Integer instructorId;

}
