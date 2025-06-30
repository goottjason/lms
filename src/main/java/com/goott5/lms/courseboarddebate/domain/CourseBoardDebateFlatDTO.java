package com.goott5.lms.courseboarddebate.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CourseBoardDebateFlatDTO {
  // CourseBoardMaterials 정보
  private int id;
  private int courseId;
  private String courseName;
  private String title;
  private String content;
  private int readCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // User 정보 (UserVO 대신 평면적으로 받음)
  private int writerId;
  private String writerName;
  private String writerType;

}
