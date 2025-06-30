package com.goott5.lms.courseboarddebate.domain;

import com.goott5.lms.user.domain.UserVO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CourseBoardDebateCommentDTO {

  private int id;
  private int courseForumId;
  private String content;
  private int commenterId;
  private String commenterName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  private String profileImg;

}
