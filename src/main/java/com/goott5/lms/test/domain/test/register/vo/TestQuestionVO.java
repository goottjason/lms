package com.goott5.lms.test.domain.test.register.vo;

import com.goott5.lms.test.domain.test.detail.result.vo.TestOptionResultVO;
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
public class TestQuestionVO {

  private int id;
  private int testId;
  private String title;
  private String questionType;
  private int questionNo;
  private int score;
  private String correctAnswer;

  List<TestOptionVO> options;



}
