package com.goott5.lms.coursemanagement.domain.integrated;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLearnerOverviewResp<LearnerOverviewResp> {
  private List<LearnerOverviewResp> learnerList;
  private Integer totalCount;
  private Double courseAvgAttendanceRate;
}
