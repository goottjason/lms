package com.goott5.lms.homework.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import lombok.Generated;
import software.amazon.awssdk.annotations.NotNull;

@EqualsAndHashCode
@Builder
@Data
public class HomeworkSubmissionDTO {

  @Generated
  private Integer id;

  @NotNull
  private Integer homeworkId;

  @NotBlank(message = "제목을 입력해주세요.")
  private String title;

  private String content;

  private Integer readCount;
  private Integer learnerId;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

//  @Builder.Default
//  private PagingRequestDTO pagingRequest = PagingRequestDTO.builder()
//      .pageNo(1)
//      .pageNo(5)
//      .build();



}
