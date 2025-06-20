package com.goott5.lms.communityInquiry.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class InquiryVO {

  private int id;
  private String title;
  private String inquiry;
  private int writer;
  private String writerName;
  private boolean isPosted;
  private boolean isAnswered;
  private boolean isAnsweredChecked;
  private int answerer;
  private String answererName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  private LocalDateTime answeredAt;

}
