package com.goott5.lms.userregister.domain;

import java.time.LocalDate;
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
public class UserDTO {

  private int id;
  private String type;
  private String fullname;
  private String email;
  private LocalDate birthday;
  private String gender;
  private String position;
  private LocalDate hireDate;
}
