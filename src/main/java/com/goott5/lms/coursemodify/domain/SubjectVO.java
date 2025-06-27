package com.goott5.lms.coursemodify.domain;

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
public class SubjectVO {

  private int id;
  private int courseId;
  private int subjectOrder;
  private String name;
  private int hours;
  private String textbookName;
  private String textbookAuthor;



}
