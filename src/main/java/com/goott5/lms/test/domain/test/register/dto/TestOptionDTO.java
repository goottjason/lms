package com.goott5.lms.test.domain.test.register.dto;

import jakarta.validation.constraints.AssertTrue;
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
public class TestOptionDTO {

  private int id;
  private int questionId;

  @Min(value = 1, message = "번호는 1부터 시작합니다.")
  @Max(value = 4, message = "최대 4개까지 입력 가능합니다.")
  private int optionNo;

  @NotBlank(message = "내용을 입력하세요.")
  private String optionContent;
  private boolean isCorrect;

  public boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

}
