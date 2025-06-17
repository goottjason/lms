package com.goott5.lms.learnermanagement.domain;

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
public class PageUserReqDTO<T> {

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
