package com.goott5.lms.coursemanagement.domain.integrated;

import com.goott5.lms.coursemanagement.domain.table.CourseSchedule;
import com.goott5.lms.coursemanagement.domain.table.CourseSubject;
import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerResponse;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import java.time.LocalDate;
import java.util.List;
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

  // 교육생 전체 정보
  private CourseLearnerOverviewResp<LearnerOverviewResp> courseLearnerOverview;

  private List<LocalDate> courseTrainingDates;

}
