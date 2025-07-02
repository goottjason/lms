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
public class QnAVO {

  private int id;
  private String title;
  private Boolean isAnswer;

  public Boolean getIsAnswer() {
    return isAnswer;
  }

  public void setIsAnswer(Boolean isAnswer) {
    this.isAnswer = isAnswer;
  }

}
