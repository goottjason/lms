package com.goott5.lms.test.domain.learnermain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TestVO {

  private int testId;
  private String title;
  private double avgScore;
  private int myScore;
  private double percentile;

}
