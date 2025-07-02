package com.goott5.lms.learnermanagement.domain.integrated;

import java.util.List;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationOverviewResp<ParticipationWithReason> {

  private List<ParticipationWithReason> partList;
  private Integer totalCount;
  private Map<String, Integer> statusCount;
  private Double attendanceRate;
  private Boolean isCompletionAboutParticipation;
}
