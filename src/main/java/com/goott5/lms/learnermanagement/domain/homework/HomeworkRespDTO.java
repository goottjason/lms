package com.goott5.lms.learnermanagement.domain.homework;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkRespDTO {
  private Integer homeworkId;
  private Integer hsId;
  private Integer heId;
  private Boolean heIsPass;
}
