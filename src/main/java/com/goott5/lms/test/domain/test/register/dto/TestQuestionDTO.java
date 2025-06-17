package com.goott5.lms.test.domain.test.register.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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
public class TestQuestionDTO {

  private int id;
  private int testId;


  private int questionNo;

  @NotBlank(message = "문항 내용을 입력하세요.")
  private String questionTitle;
  private String questionType;

  @Min(value = 1, message = "배점은 최소 1점")
  private int questionScore;

  private String questionAnswer;

  @Valid
  private List<TestOptionDTO> options;

  @AssertTrue(message = "주관식 정답을 입력하세요")
  private boolean isShortAnswerValid() {

    if (!"SHORT".equals(questionType)) {
      return true;
    }

    return questionAnswer != null && !questionAnswer.trim().isEmpty();
  }

  @AssertTrue(message = "정답을 하나 선택해 주세요.")
  private boolean isMultipleAnswerValid() {
    if (!"MULTIPLE".equals(questionType)) {
      return true;
    }

    if (options == null) {
      return false;
    }

    for (TestOptionDTO option : options) {
      if (option.getIsCorrect()) {
        return true;
      }
    }

    return false;
  }


}
