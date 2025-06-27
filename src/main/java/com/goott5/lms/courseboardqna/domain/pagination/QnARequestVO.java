package com.goott5.lms.courseboardqna.domain.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class QnARequestVO {

  @Builder.Default
  @Min(value = 1)
  @Positive
  private int currentPageNo = 1;

  @Builder.Default
  @Min(value = 1)
  @Max(value = 100)
  @Positive
  private int pageSize = 10;

  @Builder.Default
  @Min(value = 1)
  @Positive
  private int currentPageGroup = 1;

  @Builder.Default
  @Positive
  private int pagesPerGroup = 10;

  private int totalItemsCount;

  private SearchDTO searchOptions;

  public int getSkip() {
    return (currentPageNo - 1) * pageSize;
  }


}
