package com.goott5.lms.operationsmanagement.domain;

import jakarta.validation.Valid;
import java.util.List;
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
public class PageStaffHistoryReqDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private String type;
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

  @Valid
  private List<T> staffHistories;

  private Integer staffId;
  /*private String staffType;
  private String staffPosition;
  private Boolean includeLeaveDate;*/
}
