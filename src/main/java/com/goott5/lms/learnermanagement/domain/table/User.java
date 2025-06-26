package com.goott5.lms.learnermanagement.domain.table;

import java.time.LocalDateTime;
import java.util.Date;
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
public class User {
  // user 테이블 정보
  private Integer userId;
  private String userType;
  private String userLoginId;
  private String userPassword;
  private String userFullname;
  private Character userGender;
  private Date userBirthday;
  private String userMobile;
  private String userEmail;
  private String userAddress;
  private String userProfileImg;
  private String userSessionId;
  private LocalDateTime userAutoLoginLimit;
  private Integer userWrongPasswordCount;
  private LocalDateTime userCreatedAt;
  private LocalDateTime userUpdatedAt;
  private LocalDateTime userDeletedAt;
}
