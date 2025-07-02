package com.goott5.lms.test.domain.learnermain;

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
@ToString
@Builder
public class InquiryVO {

  private int id;
  private String title;
  private Boolean isAnswered;
  private Boolean isAnsweredChecked;

  public Boolean getIsAnswered() {
    return isAnswered;
  }

  public void setIsAnswered(Boolean isAnswered) {
    this.isAnswered = isAnswered;
  }

  public Boolean getIsAnsweredChecked() {
    return isAnsweredChecked;
  }

  public void setIsAnsweredChecked(Boolean isAnsweredChecked) {
    this.isAnsweredChecked = isAnsweredChecked;
  }

}
