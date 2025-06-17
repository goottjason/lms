package com.goott5.lms.learnermanagement.domain.participation;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageParticipationReqDTO<T> {
  private Integer pageNo;
  private Integer pageSize;

  private String type;
  private String keyword;

  @Builder.Default
  private String orderBy = "participation_date";
  @Builder.Default
  private String orderDirection = "DESC";

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
