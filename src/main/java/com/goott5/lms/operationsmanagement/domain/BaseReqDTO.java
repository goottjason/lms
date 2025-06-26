package com.goott5.lms.operationsmanagement.domain;

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
public class BaseReqDTO {
  private Integer loginUserId;
  private String loginUserType;
  private String loginUserPosition;
}
