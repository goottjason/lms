package com.goott5.lms.learnermanagement.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartModifyRequest {
  private Integer loginUserId;
  private String loginUserType;
  private String loginUserPosition;
  private Integer partId;
  private LocalDateTime partCheckIn;
  private LocalDateTime partCheckOut;
  private String partStatus;
  private String partExplanation;
  private Integer partTrainingTime;
}
