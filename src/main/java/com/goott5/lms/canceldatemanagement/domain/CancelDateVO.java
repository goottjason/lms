package com.goott5.lms.canceldatemanagement.domain;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelDateVO {

  private int id;
  private boolean isAll;
  private int courseId;
  private boolean isPublicHoliday;
  private LocalDate cancelDate;
  private String reason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  private String name;
}
