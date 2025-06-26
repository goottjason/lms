package com.goott5.lms.operationsmanagement.domain;

import java.time.LocalDate;
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
public class StaffReqDTO {
  private Integer userId;
  private String type;
  private String loginId;
  private String password;
  private String fullname;
  private String gender;
  private LocalDate birthday;
  private String mobile;
  private String email;
  private String address;
  private String profileImg;
  private String sessionId;
  private LocalDateTime autoLoginLimit;
  private Integer wrongPasswordCount;
}
