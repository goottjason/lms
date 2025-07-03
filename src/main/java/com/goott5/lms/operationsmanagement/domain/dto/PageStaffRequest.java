package com.goott5.lms.operationsmanagement.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageStaffRequest {
  private Integer pageNo;
  private Integer pageSize;

  @Builder.Default
  private String type = "fullname";
  private String keyword;

  @Builder.Default
  private String orderBy = "fullname";
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

  private Integer staffId;
  private String staffType;
  private String staffPosition;
  private Boolean onlyLeaver;
}
