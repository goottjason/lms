package com.goott5.lms.learnermanagement.domain.table;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentSupport {

  private Integer esId;
  private Integer esLearnerEnrollmentId;
  private Boolean esIsCounselingReceived;
  private String esCounselingDetails;
  private String esEmploymentStatus;
  private String esCompanyName;
  private String esCompanyPhone;
  private String esCompanyAddress;
  private LocalDateTime esCreatedAt;
  private LocalDateTime esUpdatedAt;
  private LocalDateTime esDeletedAt;
}
