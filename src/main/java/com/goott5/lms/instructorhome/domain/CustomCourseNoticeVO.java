package com.goott5.lms.instructorhome.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomCourseNoticeVO {

  private int id;
  private int writerId;
  private String writerName;
  private String title;
  private LocalDate createdAt;
  private boolean isFixed;


}
