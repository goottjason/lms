package com.goott5.lms.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class SignupDTO {

  private int id;
  private String loginId;
  private String fullname;
  private String password;
  private String email;
  private String mobile;
  private String address;
  private MultipartFile profileFile;
  private String profileImg;
}
