package com.goott5.lms.test.domain.test.list;

import java.time.LocalDateTime;

public enum TestStatus {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED;

  public static TestStatus determineStatus(LocalDateTime startDate, LocalDateTime endDate) {
    LocalDateTime now = LocalDateTime.now();

    if (now.isBefore(startDate)) {
      return NOT_STARTED;
    } else if (now.isAfter(endDate)) {
      return COMPLETED;
    } else {
      return IN_PROGRESS;
    }
  }
}
