package com.goott5.lms.coursemanagement.domain.table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSchedule {
  private Integer csId;
  private Integer csSubjectId;
  private Integer csCourseId;
  private LocalDate csClassDate;
  private Integer csPeriod;
  private LocalTime csPeriodStartTime;
  private LocalTime csPeriodEndTime;
  private LocalDateTime csCreatedAt;
  private LocalDateTime csUpdatedAt;
  private LocalDateTime csDeletedAt;


}
