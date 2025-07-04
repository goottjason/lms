package com.goott5.lms.test.domain.learnermain;

import java.time.LocalDateTime;
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
@ToString
@Builder
public class ForumVO {

  private int id;
  private String title;
  private String fullname;
  private int forumLike;
  private LocalDateTime createdAt;
  private int commentCount;

}
