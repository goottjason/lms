package com.goott5.lms.learnermanagement.domain.table;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestWithSub {

  private Integer teId;
  private Integer teInstructorId;
  private Integer teCourseId;
  private String teTitle;
  private LocalDateTime teStartDate;
  private LocalDateTime teEndDate;
  private Integer teTestTime;
  private Integer teTotalScore;
  private LocalDateTime teCreatedAt;
  private LocalDateTime teUpdatedAt;
  private LocalDateTime teDeletedAt;

  private Integer tsId;
  private Integer tsTestId;
  private Integer tsLearnerId;
  private Integer tsSubmissionTime;
  private String tsSubmissionStatus;
  private Integer tsRetryCount;
  private Boolean tsIsInvalidated;
  private LocalDateTime tsSubmissionRegDate;
  private Integer tsScore;
  private LocalDateTime tsCreatedAt;
  private LocalDateTime tsUpdatedAt;
  private LocalDateTime tsDeletedAt;
}
