package com.goott5.lms.test.domain.test.register.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class TestRegisterDTO {

  private int id;
  private int instructorId;
  private String courseName;

  @NotBlank(message = "시험 제목을 입력하세요.")
  private String testTitle;

  @NotBlank(message = "시작일을 입력하세요.")
  private String startDate;

  @NotBlank(message = "종료일을 입력하세요.")
  private String endDate;

  @Min(value = 10, message = "시험 시간은 10분 이상")
  @Max(value = 100, message = "시험 시간은 최대 100분까지 가능합니다.")
  private int testTime;

  @Min(value = 100, message = "총 배점은 반드시 100점이어야 합니다.")
  @Max(value = 100, message = "총 배점은 반드시 100점이어야 합니다.")
  private int totalScore;

  @Valid
  private List<TestQuestionDTO> questions;

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");

  @AssertTrue(message = "시험 시간은 시작~종료 시간보다 짧아야 합니다.")
  public boolean isTestTimeValid() {
    if (startDate == null || startDate.isBlank() ||
        endDate == null || endDate.isBlank()) {
      return true;
    }
    // testTime 최소 10분 검사
    if (testTime < 10) {
      return false;
    }
    LocalDateTime start = LocalDateTime.parse(startDate, FORMATTER);
    LocalDateTime end = LocalDateTime.parse(endDate, FORMATTER);

    Duration duration = Duration.between(start, end);
    int minutes = (int) duration.toMinutes();
    return (minutes >= testTime);
  }


  @AssertTrue(message = "시작일은 미래여야 합니다.")
  public boolean isStartDateInFuture() {
    if (startDate == null || startDate.trim().isEmpty()) {
      return true;
    }

    LocalDateTime start = LocalDateTime.parse(startDate, FORMATTER);
    return start.isAfter(LocalDateTime.now());
  }

  @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
  public boolean isEndDateAfterStartDate() {
    if ((startDate == null || startDate.trim().isEmpty()) || (endDate == null || endDate.trim()
        .isEmpty())) {
      return true;
    }

    LocalDateTime start = LocalDateTime.parse(startDate, FORMATTER);
    LocalDateTime end = LocalDateTime.parse(endDate, FORMATTER);

    if (end.getMinute() % 10 == 0) {
      return true;
    }

    return end.isAfter(start);
  }

  @AssertTrue(message = "시작 시간은 10분 단위여야 합니다.")
  public boolean isStartDateValid() {

    if (startDate == null || startDate.trim().isEmpty()) {
      return true;
    }

    LocalDateTime start = LocalDateTime.parse(startDate, FORMATTER);

    if (!start.isAfter(LocalDateTime.now())) {
      return true;
    }

    return start.getMinute() % 10 == 0;
  }

  @AssertTrue(message = "종료 시간은 10분 단위여야 합니다.")
  public boolean isEndDateValid() {
    if (endDate == null || endDate.trim().isEmpty()) {
      return true;
    }

    LocalDateTime start = LocalDateTime.parse(startDate, FORMATTER);
    LocalDateTime end = LocalDateTime.parse(endDate, FORMATTER);

    if (!end.isAfter(start)) {
      return true;
    }

    return end.getMinute() % 10 == 0;
  }

}
