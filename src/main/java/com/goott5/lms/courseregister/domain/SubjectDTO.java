package com.goott5.lms.courseregister.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class SubjectDTO {

  private int id;
  private int course_id;
  private int subjectOrder;
  private String name;
  private int hours;
  private String textbookName;
  private String textbookAuthor;

}
