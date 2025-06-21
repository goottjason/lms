package com.goott5.lms.test.domain.test.detail.result.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class TestOptionResultDTO {

  private int id;
  private int questionId;
  private int optionNo;
  private String optionContent;
  private boolean isCorrect;

  public boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

}
