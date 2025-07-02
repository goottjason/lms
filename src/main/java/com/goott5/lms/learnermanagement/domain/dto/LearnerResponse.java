package com.goott5.lms.learnermanagement.domain.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerResponse {
  private Integer userId;
  private String userFullname;
  // le 테이블 정보(1:1)
  private Integer leId;
  private Integer leUserId;
  private Integer leCourseId;
  private String leCompletionStatus;

  private Boolean coInProgress;

  private Integer coInstructorId;
  private String coInstructorName;
  private Integer coCourseHeadId;
  private String coCourseHeadName;
  private Integer coClassroomId;
  private String coClassroomName;
}
