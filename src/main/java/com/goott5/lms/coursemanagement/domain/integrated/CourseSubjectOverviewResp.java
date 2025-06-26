package com.goott5.lms.coursemanagement.domain.integrated;

import com.goott5.lms.coursemanagement.domain.table.CourseSubject;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSubjectOverviewResp<T> {
  private List<CourseSubject> subjectList;
  private Integer totalCount;
}
