package com.goott5.lms.operationsmanagement.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StaffHistoryResp {
  private Integer courseId;
  private String courseName;
  private Boolean courseIsInProgress;
  private LocalDate courseStartDate;
  private LocalDate courseEndDate;
  private Integer courseNumberOfLearner;

}
