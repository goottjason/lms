package com.goott5.lms.coursemanagement.domain.integrated;

import com.goott5.lms.coursemanagement.domain.table.CourseSchedule;
import com.goott5.lms.coursemanagement.domain.table.CourseSubject;
import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseOverviewResp {

  // course 테이블 정보(1:1)
  private CourseWithAssignedInfo courseWithAssignedInfo;

  // course_subject 테이블 정보(1:N)
  private CourseSubjectOverviewResp<CourseSubject> subjectOverview;

  // course_schedule 테이블 정보 (1:N)
  private CourseScheduleOverviewResp<CourseSchedule> scheduleOverview;

}
