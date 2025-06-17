package com.goott5.lms.coursemanagement.domain;

import java.time.LocalDateTime;
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
public class CourseSubjectRespDTO {

  private Integer id;
  private Integer courseId;
  private Integer subjectOrder;
  private String name;
  private Integer hours;
  private String textbookName;
  private String textbookAuthor;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

}
