package com.goott5.lms.learnermanagement.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageLearnerRespDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<T> respDTOS;

  @Builder(builderMethodName = "withPageInfo")
  public PageLearnerRespDTO(PageLearnerReqDTO pageLearnerReqDTO, List<T> respDTOS, int totalRecords) {

    this.totalRecords = totalRecords;

    this.pageNo = pageLearnerReqDTO.getPageNo();
    this.pageSize = pageLearnerReqDTO.getPageSize();

    if (pageNo != null || pageSize != null) {

      this.blockEndPage = (((this.pageNo - 1) / this.pageSize) + 1) * this.pageSize;
      this.blockStartPage = this.blockEndPage - (this.pageSize - 1);
      this.lastPage = (int) (Math.ceil(this.totalRecords / (double) pageSize));

      this.blockEndPage = Math.min(this.blockEndPage, this.lastPage);

      if (this.blockEndPage == 0) {
        this.blockEndPage = 1;
      }
    }

    this.respDTOS = respDTOS;
  }

}
