package com.goott5.lms.test.domain.test.answer;

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
public class QuestionScoreVO {

  private int questionNo;
  private int score;
  private boolean isCorrect;

}
