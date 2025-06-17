package com.goott5.lms.learnermanagement.domain.test;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRespDTO {

  private Integer testId;
  private Integer tsId;
  private String tsSubmissionStatus;
  private Integer tsScore;
  private Boolean tsIsInvalidated;
}
