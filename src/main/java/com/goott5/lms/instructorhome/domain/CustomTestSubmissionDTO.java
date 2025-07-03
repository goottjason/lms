package com.goott5.lms.instructorhome.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomTestSubmissionDTO {

  private int testSubmissionId;
  private int learnerId;
  private String learnerName;
  private int score;

}
