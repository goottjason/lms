package com.goott5.lms.canceldatemanagement.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PagingRequestDTO {

  @Builder.Default
  @Min(value = 1)
  @Positive
  private int pageNo = 1;

  @Builder.Default
  @Min(value = 10)
  @Max(value = 100)
  @Positive
  private int pagingSize = 10;

  private int categorizeType; // 분류타입 : 0(전부), 1(진행상황별), 2(과정별)
  private int firstCategory;
  private int secondCategory;

  public int getSkip() {
    return (pageNo - 1) * pagingSize;
  }


}
