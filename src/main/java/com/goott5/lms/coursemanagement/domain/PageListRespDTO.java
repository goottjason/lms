package com.goott5.lms.coursemanagement.domain;

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
public class PageListRespDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;
  private boolean showPrevBlockButton;
  private boolean showNextBlockButton;

  private List<T> respDTOS;

  @Builder(builderMethodName = "withPageInfo")
  public PageListRespDTO(PageListReqDTO pageListReqDTO, List<T> respDTOS, int totalRecords) {

    this.totalRecords = totalRecords;

    this.pageNo = pageListReqDTO.getPageNo();
    this.pageSize = pageListReqDTO.getPageSize();

    this.blockEndPage = (((this.pageNo - 1) / this.pageSize) + 1) * this.pageSize;
    this.blockStartPage = this.blockEndPage - (this.pageSize - 1);
    this.lastPage = (int) (Math.ceil(this.totalRecords / (double) pageSize));

    this.blockEndPage = Math.min(this.blockEndPage, this.lastPage);

    if (this.blockEndPage == 0) {
      this.blockEndPage = 1;
    }

    this.showPrevBlockButton = this.blockStartPage > 1;
    this.showNextBlockButton = this.blockEndPage < this.lastPage;

    this.respDTOS = respDTOS;
  }

}
