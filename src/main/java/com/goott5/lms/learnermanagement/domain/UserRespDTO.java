package com.goott5.lms.learnermanagement.domain;

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
public class UserRespDTO {

  private Integer id;
  private String type;
  private String loginId;
  private String password;
  private String fullname;
  private Character gender;
  private Date birthday;
  private String mobile;
  private String email;
  private String address;
  private String profileImg;
  private String sessionId;
  private LocalDateTime autoLoginLimit;
  private Integer wrongPasswordCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  private Integer courseId;
  private String courseName;
  private Integer leId;
  private String completionStatus;
}
