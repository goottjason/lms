package com.goott5.lms.employmentsupport.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmploymentVO {

  private int id;
  private int learnerEnrollmentId;
  private int isCounselingReceived;
  private String counselingDetails;
  private String employmentStatus;
  private String companyName;
  private String companyPhone;
  private String companyAddress;


}
