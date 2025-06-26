package com.goott5.lms.coursemanagement.domain.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageCourseRequest {
  private Integer pageNo;
  private Integer pageSize;

  private String type;
  private String keyword;

  @Builder.Default
  private String orderBy = "coStartDate";
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

  private Integer coId;
  private Boolean coIsInProgress;
}
