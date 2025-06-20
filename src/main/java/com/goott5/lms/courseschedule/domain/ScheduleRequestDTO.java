package com.goott5.lms.courseschedule.domain;

import java.time.LocalDate;
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
@Builder
@ToString
public class ScheduleRequestDTO {

  private int courseId;
  private LocalDate weekStart;
  private LocalDate weekEnd;

}
