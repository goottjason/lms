package com.goott5.lms.courseboardmaterials.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseBoardMaterialsVO {

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
