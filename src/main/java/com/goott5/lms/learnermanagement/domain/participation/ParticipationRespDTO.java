package com.goott5.lms.learnermanagement.domain.participation;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRespDTO {

  private Integer pId;
  private Integer prId;
  private String pStatus;
  private LocalDate pParticipationDate;
  private Integer pTrainingTime;
  private String prExplanation;

}
