package com.goott5.lms.test.domain.test.register.vo;

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
public class TestRegisterVO {

  private int id;
  private int instructorId;
  private int courseId;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private int testTime;
  private int totalScore;

  List<TestQuestionVO> questions;

}
