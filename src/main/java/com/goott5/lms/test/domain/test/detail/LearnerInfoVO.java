package com.goott5.lms.test.domain.test.detail;

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
public class LearnerInfoVO {

  private int learnerId;
  private String fullName;
  private String loginId;
  private String submissionStatus;
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
