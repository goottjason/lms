package com.goott5.lms.coursemodify.domain;

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
public class CourseResponseDTO {

  private int id;
  private int isInProgress;
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
  private int instructorId;
  private String fulltimeInstructorFullname;
  private int administratorId;
  private String courseHeadFullname;
  private int classroomId;
  private String classroomName;


  private List<SubjectVO> subjects;



}
