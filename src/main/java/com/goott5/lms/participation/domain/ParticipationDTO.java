package com.goott5.lms.participation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ParticipationDTO {

  private Integer id;
  private Integer learnerEnrollmentId;
  private String status;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer trainingTime;
  private LocalDate participationDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  // 화면 표시용
  private String displayStatus;
  private String displayStatusText;
  private Boolean isStatusVisible;
}
