package com.goott5.lms.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UserVO {

  private int id;
  private String type;
  private String loginId;
  private String password;
  private String fullName;
  private String gender;
  private LocalDate birthday;
  private String mobile;
  private String email;
  private String address;
  private String profileImg;
  private String sessionId;
  private LocalDateTime autoLoginLimit;
  private int wrongPasswordCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

}
