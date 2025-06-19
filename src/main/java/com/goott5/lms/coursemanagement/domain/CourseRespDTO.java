package com.goott5.lms.coursemanagement.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
public class CourseRespDTO {

  private Integer id;
  private Boolean isInProgress; // TINYINT → Boolean으로 매핑 가능
  private String name;
  private Integer numberOfLearner;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer totalHours;
  private Integer totalDays;
  private Integer dailyHours;
  private Integer breakTime;
  private LocalTime lessonStartTime;
  private LocalTime lessonEndTime;
  private LocalTime lunchStartTime;
  private LocalTime lunchEndTime;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  // Classroom 정보
  private Integer classroomId;
  private String classroomName;

  // 학과장/강사 정보
  private Integer courseHeadId;
  private String courseHeadFullname;
  private Integer fulltimeInstructorId;
  private String fulltimeInstructorFullname;

  private List<CourseSubjectRespDTO> subjects;
}
