package com.goott5.lms.learnermanagement.domain.table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationWithReason {
  private Integer partId;
  private Integer partLearnerEnrollmentId;
  private String partStatus;
  private LocalDateTime partCheckIn;
  private LocalDateTime partCheckOut;
  private Integer partTrainingTime;
  private LocalDate partParticipationDate;
  private LocalDateTime partCreatedAt;
  private LocalDateTime partUpdatedAt;
  private LocalDateTime partDeletedAt;

  private String partExplanation;

}
