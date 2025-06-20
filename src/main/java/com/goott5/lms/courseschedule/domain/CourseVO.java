package com.goott5.lms.courseschedule.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourseVO {

  private int id;
  private boolean isInProgress;
  private String name;
  private int numberOfLearner;
  private LocalDate startDate;
  private LocalDate endDate;
  private int totalHours;
  private int totalDays;
  private int dailyHours;
  private int breakTime;
  private LocalTime lessonStartTime;
  private LocalTime lessonEndTime;
  private LocalTime lunchStartTime;
  private LocalTime lunchEndTime;


}
