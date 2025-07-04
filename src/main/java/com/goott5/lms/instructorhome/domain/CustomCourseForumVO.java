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
public class CustomCourseForumVO {

  private int id;
  private String title;
  private int writerId;
  private String writerName;
  private int forumLike;
  private LocalDate createdAt;
  private int commentCount;
}
