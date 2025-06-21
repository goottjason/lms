package com.goott5.lms.test.domain.test.answer;

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
public class TestAnswerDTO {

  private int testId;
  private int submissionTime;

  private List<Integer> questionNums;
  private List<String> selectAnswers;

}
