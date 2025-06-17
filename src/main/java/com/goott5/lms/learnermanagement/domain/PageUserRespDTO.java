package com.goott5.lms.learnermanagement.domain;

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
public class PageUserRespDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;
  private Boolean showPrevBlockButton;
  private Boolean showNextBlockButton;

  private List<T> respDTOS;

  @Builder(builderMethodName = "withPageInfo")
  public PageUserRespDTO(PageUserReqDTO pageUserReqDTO, List<T> respDTOS, int totalRecords) {

    this.totalRecords = totalRecords;

    this.pageNo = pageUserReqDTO.getPageNo();
    this.pageSize = pageUserReqDTO.getPageSize();

    if (pageNo != null || pageSize != null) {

      this.blockEndPage = (((this.pageNo - 1) / this.pageSize) + 1) * this.pageSize;
      this.blockStartPage = this.blockEndPage - (this.pageSize - 1);
      this.lastPage = (int) (Math.ceil(this.totalRecords / (double) pageSize));

      this.blockEndPage = Math.min(this.blockEndPage, this.lastPage);

      if (this.blockEndPage == 0) {
        this.blockEndPage = 1;
      }

      this.showPrevBlockButton = this.blockStartPage > 1;
      this.showNextBlockButton = this.blockEndPage < this.lastPage;
    }

    this.respDTOS = respDTOS;
  }

}
