package com.goott5.lms.learnermanagement.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentSupportUpdateReqDTO {
  private Integer loginUserId;
  private String loginUserType;
  private Integer leId;
  private EmploymentSupportReqDTO reqDTO;
}
