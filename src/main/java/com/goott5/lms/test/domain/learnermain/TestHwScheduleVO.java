package com.goott5.lms.test.domain.learnermain;

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
public class TestHwScheduleVO {

  private int id;
  private String title;
  private LocalDateTime startDate;
  private String dDay;
  private String submissionStatus;
  private Boolean isInvalidated;
  private int score;
  private String contentType;

  public Boolean getIsInvalidated() {
    return isInvalidated;
  }

  public void setIsInvalidated(Boolean invalidated) {
    isInvalidated = invalidated;
  }

}
