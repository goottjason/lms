package com.goott5.lms.homework.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@Data
public class HomeworkModifyDTO {

  @Generated
  private Integer id;

  @NotBlank(message = "제목을 입력하세요.")
  private String title;

  @NotNull(message = "제출 시작 날짜를 입력하세요.")
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startDate;

  @NotNull(message = "제출 마감 날짜를 입력하세요.")
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endDate;

  private String content;

  private OffsetDateTime updatedAt;




}
