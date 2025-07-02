package com.goott5.lms.operationsmanagement.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomUsageResp {
  private String classroomName;
  private String courseName;
  private LocalDate startDate;
  private LocalDate endDate;
}
