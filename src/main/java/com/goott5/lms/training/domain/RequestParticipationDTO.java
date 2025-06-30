package com.goott5.lms.training.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestParticipationDTO {

  private String status;
  private Date trainingDate;
  private Integer courseId;

}
