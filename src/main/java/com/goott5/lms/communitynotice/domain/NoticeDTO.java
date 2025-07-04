package com.goott5.lms.communitynotice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {

  private int id;
  private int writerId;

  @NotBlank(message = "* 제목은 필수 입력 항목입니다.")
  @Size(min = 1, max = 100, message = "* 제목은 1자 이상 100자 이하로 입력해주세요.")
  private String title;

  @NotBlank(message = "* 내용은 필수 입력 항목입니다.")
  @Size(min = 1, max = 1000, message = "* 내용은 1자 이상 1000자 이하로 입력해주세요.")
  private String content;

  private int readCount;
  private Boolean isFixed;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  private String writerName;
  private boolean isAttached;
}