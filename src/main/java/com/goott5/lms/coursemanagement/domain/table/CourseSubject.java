package com.goott5.lms.coursemanagement.domain.table;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSubject {
  private Integer cuId;
  private Integer cuCourseId;
  private Integer cuSubjectOrder;
  private String cuName;
  private Integer cuHours;
  private String cuTextbookName;
  private String cuTextbookAuthor;
  private LocalDateTime cuCreatedAt;
  private LocalDateTime cuUpdatedAt;
  private LocalDateTime cuDeletedAt;

}
