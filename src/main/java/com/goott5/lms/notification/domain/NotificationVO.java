package com.goott5.lms.notification.domain;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {

  private int id;
  private int userId;
  private String content;
  private String targetURI;
  private boolean isChecked;
  private boolean isWarning;
  private LocalDate createdAt;

}
