package com.goott5.lms.test.domain.learnermain;

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
@ToString
@Builder
public class AttendanceStatusVO {

  private String status;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;

}
