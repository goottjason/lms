package com.goott5.lms.courseschedule.domain;

import java.util.List;
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
public class ScheduleResponseDTO {

  private List<ScheduleVO> scheduleVOS;
  private boolean prev;
  private boolean next;

}
