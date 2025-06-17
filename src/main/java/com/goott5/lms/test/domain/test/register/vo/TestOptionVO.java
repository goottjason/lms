package com.goott5.lms.test.domain.test.register.vo;

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
public class TestOptionVO {

  private int id;
  private int questionId;
  private int optionNo;
  private String content;
  private boolean isCorrect;

  public boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(boolean isCorrect) {
    this.isCorrect = isCorrect;
  }


}
