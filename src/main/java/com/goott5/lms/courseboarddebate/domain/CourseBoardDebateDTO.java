package com.goott5.lms.courseboarddebate.domain;

import jakarta.validation.constraints.NotBlank;
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
public class CourseBoardDebateDTO {

  private int id;
  private int courseId;
  private int writerId;
  private String courseName;
  private String writerName;
  private int readCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  private String title;

  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  private String content;

}
