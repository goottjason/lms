package com.goott5.lms.learnermanagement.domain.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageLearnerRequest<T> {
  private Integer pageNo;
  private Integer pageSize;

  private String type;
  private String keyword;

  private String orderBy;
  private String orderDirection;

  private List<T> requestList;

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

}
