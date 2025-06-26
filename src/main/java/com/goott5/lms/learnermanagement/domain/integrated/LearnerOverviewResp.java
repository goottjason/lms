package com.goott5.lms.learnermanagement.domain.integrated;

import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.learnermanagement.domain.table.EmploymentSupport;
import com.goott5.lms.learnermanagement.domain.table.HomeworkWithSubEval;
import com.goott5.lms.learnermanagement.domain.table.ParticipationWithReason;
import com.goott5.lms.learnermanagement.domain.table.TestWithSub;
import com.goott5.lms.learnermanagement.domain.table.User;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerOverviewResp {

  // user 테이블 정보(1:1)
  private User learnerUser;

  // course 테이블 정보(1:1)
  private CourseWithAssignedInfo learnerCourse;

  // le 테이블 정보(1:1)
  private Integer leId;
  private Integer leUserId;
  private Integer leCourseId;
  private String leCompletionStatus;

  // es 테이블 정보(1:1)
  private EmploymentSupport learnerEmploymentSupport;

  // pa, pr 테이블 정보(1:N)
  private ParticipationOverviewResp<ParticipationWithReason> partOverview;

  // ho, hs, he 테이블 정보(1:N)
  private HomeworkOverviewResp<HomeworkWithSubEval> homeOverview;

  // te, ts 테이블 정보(1:N)
  private TestOverviewResp<TestWithSub> testOverview;


}
