package com.goott5.lms.homework.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode
@Builder
@Data
public class HomeworkSubmissionDTO {

  private int id;
  private int homeworkId;
  private String title;
  private String content;
  private int readCount;
  private int learnerId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

//  @Builder.Default
//  private PagingRequestDTO pagingRequest = PagingRequestDTO.builder()
//      .pageNo(1)
//      .pageNo(5)
//      .build();



}
