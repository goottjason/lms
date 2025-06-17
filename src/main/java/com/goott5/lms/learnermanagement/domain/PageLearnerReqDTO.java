package com.goott5.lms.learnermanagement.domain;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageLearnerReqDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private String type;
  private String keyword;

  @Builder.Default
  private String orderBy = "id";
  @Builder.Default
  private String orderDirection = "ASC";

  @Valid
  private List<T> reqDTOS;

  private Integer offset;

  public Integer getOffset() {
    if (pageNo == null && pageSize == null) {
      return null;
    } else {
      return (pageNo - 1) * pageSize;
    }
  }


}
