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
public class CourseWithAssignedInfo {

  private Integer coId;
  private Boolean coIsInProgress;
  private String coName;
  private Integer coNumberOfLearner;
  private LocalDate coStartDate;
  private LocalDate coEndDate;
  private Integer coTotalHours;
  private Integer coTotalDays;
  private Integer coDailyHours;
  private Integer coBreakTime;
  private LocalTime coLessonStartTime;
  private LocalTime coLessonEndTime;
  private LocalTime coLunchStartTime;
  private LocalTime coLunchEndTime;
  private LocalDateTime coCreatedAt;
  private LocalDateTime coUpdatedAt;
  private LocalDateTime coDeletedAt;

  private Integer coInstructorId;
  private String coInstructorName;
  private Integer coCourseHeadId;
  private String coCourseHeadName;

  private Integer coClassroomId;
  private String coClassroomName;

}
