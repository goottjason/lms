package com.goott5.lms.test.domain.test.detail.result.dto;

import com.goott5.lms.test.domain.test.register.dto.TestQuestionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Builder
@ToString
public class TestRegisterResultDTO {

  private int id;
  private int instructorId;
  private String courseName;
  private String testTitle;
  private String startDate;
  private String endDate;
  private int testTime;
  private int totalScore;


  private List<TestQuestionResultDTO> questions;




}
