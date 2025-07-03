package com.goott5.lms.instructorhome.domain;

import java.util.List;
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
public class CustomTestDTO {

  private int testId;
  private int totalScore;
  private String title;
  private float average;
  private float variance;
  private float standardDeviation;
  private List<CustomTestSubmissionDTO> customTestSubmissionDTOS;

}
