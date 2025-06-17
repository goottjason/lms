package com.goott5.lms.courseregister.domain;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
public class CourseSaveDTO {

  private int id;
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

  private List<LocalDate> lessonDays;
  private int instructorId;
  private int administratorId;
  private int classroomId;
  private List<SubjectDTO> subjects;

}
