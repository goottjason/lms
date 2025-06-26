package com.goott5.lms.learnermanagement.domain.table;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkWithSubEval {
  private Integer hoId;
  private String hoTitle;
  private LocalDateTime hoStartDate;
  private LocalDateTime hoEndDate;
  private String hoContent;
  private Integer hoCourseId;
  private Integer hoReadCount;
  private Integer hoInstructorId;
  private LocalDateTime hoCreatedAt;
  private LocalDateTime hoUpdatedAt;
  private LocalDateTime hoDeletedAt;

  private Integer hsId;
  private Integer hsHomeworkId;
  private String hsTitle;
  private String hsContent;
  private Integer hsReadCount;
  private Integer hsLearnerId;
  private LocalDateTime hsCreatedAt;
  private LocalDateTime hsUpdatedAt;
  private LocalDateTime hsDeletedAt;

  private Integer heId;
  private Integer heHsId;
  private Boolean heIsPass;
  private String heContent;
  private Integer heReadCount;
  private Integer heInstructorId;
  private LocalDateTime heCreatedAt;
  private LocalDateTime heUpdatedAt;
  private LocalDateTime heDeletedAt;
}
