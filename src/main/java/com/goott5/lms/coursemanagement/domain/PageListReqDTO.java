package com.goott5.lms.coursemanagement.domain;

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
public class PageListReqDTO<T> {

  @Builder.Default
  private Integer pageNo = 1;
  @Builder.Default
  private Integer pageSize = 10;

  private String type;
  private String keyword;

  @Builder.Default
  private String orderBy = "name";
  @Builder.Default
  private String orderDirection = "ASC";

  @Valid
  private List<T> reqDTOS;

  private Integer offset;

  public Integer getOffset() {
    return (pageNo - 1) * pageSize;
  }

  // private Integer id;
}
