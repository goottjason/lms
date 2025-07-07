package com.goott5.lms.homework.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import software.amazon.awssdk.annotations.NotNull;

@Data
@Builder
public class HomeworkSubmissionForListDTO {

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

  @Nullable
  private String fullname;

  @Nullable
  private Integer hsId; //사실상 (submission)id와 동일 (he.hs_id 이므로) //null 이거나 id와 같거나



}
