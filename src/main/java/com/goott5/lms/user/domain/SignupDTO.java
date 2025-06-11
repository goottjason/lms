package com.goott5.lms.user.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

  private String userId;
  private String loginId;
  private String password;

  @NotBlank(message = "이메일은 필수 입력입니다.")
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
  private String email;

  @NotBlank(message = "휴대전화번호는 필수 입력입니다.")
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "휴대전화번호 형식에 맞지 않습니다.")
  private String mobile;

  private String address;
  private MultipartFile profileFile;
  private String profileImg;
}
