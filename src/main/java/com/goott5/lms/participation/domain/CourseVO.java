package com.goott5.lms.participation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 과정(Course) 정보 값 객체 출결 로직에서 과정별 시간정보 활용
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CourseVO {

  private Integer id;
  private Integer isInProgress;
  private String name;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer dailyHours;
  private LocalTime lessonStartTime;
  private LocalTime lessonEndTime;
  private LocalTime lunchStartTime;
  private LocalTime lunchEndTime;
  private Integer breakTime;
  private Integer totalHours; // 총 수업시간
}
