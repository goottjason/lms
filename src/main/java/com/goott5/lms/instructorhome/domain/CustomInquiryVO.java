package com.goott5.lms.instructorhome.domain;

import java.time.LocalDate;
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
public class CustomInquiryVO {

  private int id;
  private String title;
  private boolean isAnswered;
  private boolean isAnsweredChecked;
  private LocalDate createdAt;

}
