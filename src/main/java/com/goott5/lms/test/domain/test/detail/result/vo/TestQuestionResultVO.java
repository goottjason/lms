package com.goott5.lms.test.domain.test.detail.result.vo;

import com.goott5.lms.test.domain.test.register.dto.TestOptionDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TestQuestionResultVO {

  private int id;
  private int testId;
  private int questionNo;
  private String questionTitle;
  private String questionType;
  private int questionScore;
  private String questionAnswer;
  private boolean userIsCorrect;
  private String userAnswer;
  private int userScore;

  private List<TestOptionResultVO> options;


}
