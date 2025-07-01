package com.goott5.lms.training.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@Data
public class RequestParticipationDTO {

  private String status;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date trainingDate;
  private Integer courseId;

}
