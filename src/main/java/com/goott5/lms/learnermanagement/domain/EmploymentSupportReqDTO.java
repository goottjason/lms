package com.goott5.lms.learnermanagement.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentSupportReqDTO {
  private Integer esId;
  private String employmentStatus;
  private String companyName;
  private String companyPhone;
  private String companyAddress;
  private Boolean isCounselingReceived;
  private String counselingDetails;
}
