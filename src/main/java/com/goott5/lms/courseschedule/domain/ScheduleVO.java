package com.goott5.lms.courseschedule.domain;

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
public class ScheduleVO {

  private int id;
  private int courseId;
  private LocalDate classDate;
  private int period;
  private LocalTime periodStartTime;
  private LocalTime periodEndTime;
  private String subjectName;
  private String courseName;
  private String instructorName;
  private String classroomName;

}
