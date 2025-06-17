package com.goott5.lms.test.domain.test.list;

import java.time.LocalDateTime;
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
public class TestListDTO {

  private int id;
  private String title;
  private String courseName;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private TestStatus testStatus;
  private int testTime;
  private int completedCount;
  private int numberOfLearner;

}
