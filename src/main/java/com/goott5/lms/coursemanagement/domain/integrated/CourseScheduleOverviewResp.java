package com.goott5.lms.coursemanagement.domain.integrated;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseScheduleOverviewResp<CourseSchedule> {

  private List<CourseSchedule> scheduleList;
  private Integer totalCount;
  private Double courseProgressRate;
}
