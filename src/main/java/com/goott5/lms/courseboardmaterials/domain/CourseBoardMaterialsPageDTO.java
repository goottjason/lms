package com.goott5.lms.courseboardmaterials.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CourseBoardMaterialsPageDTO {

  private int id;
  private int courseId;
  private int writerId;
  private String courseName;
  private String writerName;
  private String title;
  private String content;
  private int readCount;
  private Boolean isFixed;
  private Boolean isAttached;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
