package com.goott5.lms.employmentsupport.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Builder
public class EmploymentDTO {

  private int id;
  private int isCounselingReceived;
  private String counselingDetails;
  private String employmentStatus;
  private String companyName;
  private String companyPhone;
  private String companyAddress;

}
