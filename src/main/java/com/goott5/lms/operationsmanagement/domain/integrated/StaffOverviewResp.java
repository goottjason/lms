package com.goott5.lms.operationsmanagement.domain.integrated;

import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.learnermanagement.domain.table.User;
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
public class StaffOverviewResp {
  // user 테이블 정보(1:1)
  private User staffUser;

  // course 테이블 정보(1:N)
  private List<CourseWithAssignedInfo> staffCourseList;
}
