package com.goott5.lms.test.domain.test.register.vo;

import java.time.LocalDateTime;
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
public class TestSubmissionVO {

  private int id;
  private int testId;
  private int learnerId;
  private int submissionTime;
  private String submissionStatus;
  private int retryCount;
  private boolean isInvalidated;
  private LocalDateTime submissionRegDate;
  private int score;

  public boolean getIsInvalidated() {
    return isInvalidated;
  }

  public void setIsInvalidated(boolean isInvalidated) {
    this.isInvalidated = isInvalidated;
  }

}
