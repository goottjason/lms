package com.goott5.lms.test.domain.test.detail.result.dto;

import com.goott5.lms.test.domain.test.register.dto.TestOptionDTO;
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
public class TestQuestionResultDTO {

  private int id;
  private int testId;
  private int questionNo;
  private String questionTitle;
  private String questionType;
  private int questionScore;
  private String questionAnswer;
  private String userAnswer;
  private boolean userIsCorrect;


  private List<TestOptionResultDTO> options;


}
