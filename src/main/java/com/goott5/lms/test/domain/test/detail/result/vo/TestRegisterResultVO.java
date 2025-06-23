package com.goott5.lms.test.domain.test.detail.result.vo;

import com.goott5.lms.test.domain.test.register.vo.TestQuestionVO;
import java.time.LocalDateTime;
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
public class TestRegisterResultVO {

  private String submissionTime;
  private int userScore;

  List<TestQuestionResultVO> questions;

  public void setSubmissionTime(int submissionTime) {

  }

}
