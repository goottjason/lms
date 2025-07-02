package com.goott5.lms.learnermanagement.domain.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageLearnerRequest {
  private Integer pageNo;
  private Integer pageSize;

  @Builder.Default
  private String type = "userFullname";
  private String keyword;

  @Builder.Default
  private String orderBy = "userFullname";
  @Builder.Default
  private String orderDirection = "ASC";

  private Integer offset;

  public Integer getOffset() {
    if (pageNo == null && pageSize == null) {
      return null;
    } else {
      return (pageNo - 1) * pageSize;
    }
  }

  private Boolean coIsInProgress;
  private Integer leCourseId;
  private Integer leId;

}
