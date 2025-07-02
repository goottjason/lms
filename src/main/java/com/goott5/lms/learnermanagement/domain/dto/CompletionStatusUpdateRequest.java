package com.goott5.lms.learnermanagement.domain.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionStatusUpdateRequest {
  private Integer loginUserId;
  private String loginUserType;
  private String loginUserPosition;
  private Integer leId;
  private String leCompletionStatus;
}
