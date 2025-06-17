package com.goott5.lms.courseregister.domain;

import java.time.LocalDate;
import java.time.LocalTime;
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
public class ScheduleDTO {

  private int subjectId;
  private int courseId;
  private LocalDate classDate;
  private int period;
  private LocalTime periodStartTime;
  private LocalTime periodEndTime;


}
