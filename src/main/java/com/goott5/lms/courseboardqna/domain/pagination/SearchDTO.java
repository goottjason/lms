package com.goott5.lms.courseboardqna.domain.pagination;

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
public class SearchDTO {

  private Boolean isInProgress;
  private String courseName;

  private String searchType;
  private String keyWord;
  private String answerStatus;
  private String sortBy;
  private String sortOrder;

  public Boolean getIsInProgress() {
    return isInProgress;
  }

  public void setIsInProgress(Boolean isInProgress) {
    this.isInProgress = isInProgress;
  }


}
