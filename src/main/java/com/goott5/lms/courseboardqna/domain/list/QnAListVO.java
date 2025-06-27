package com.goott5.lms.courseboardqna.domain.list;

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
@ToString
@Builder
public class QnAListVO {

  private int id;
  private String title;
  private String courseName;
  private String writerName;
  private LocalDate createdAt;
  private boolean isAnswer;
  private boolean isSecret;


}
