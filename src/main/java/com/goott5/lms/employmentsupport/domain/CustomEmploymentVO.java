package com.goott5.lms.employmentsupport.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomEmploymentVO {

  private int id;
  private int isInProgress;
  private String name;
  private String instructorFullName;
  private String learnerFullName;
  private String employmentStatus;


}
