package com.goott5.lms.operationsmanagement.domain.dto;

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
public class PageStaffResponse<T> {
  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<T> records;

  @Builder(builderMethodName = "withPageInfo")
  public PageStaffResponse(PageStaffRequest request, List<T> records, Integer totalRecords) {
    this.totalRecords = totalRecords;

    this.pageNo = request.getPageNo();
    this.pageSize = request.getPageSize();

    if (pageNo != null || pageSize != null) {

      this.blockEndPage = (((this.pageNo - 1) / this.pageSize) + 1) * this.pageSize;
      this.blockStartPage = this.blockEndPage - (this.pageSize - 1);
      this.lastPage = (int) (Math.ceil(this.totalRecords / (double) pageSize));

      this.blockEndPage = Math.min(this.blockEndPage, this.lastPage);

      if (this.blockEndPage == 0) {
        this.blockEndPage = 1;
      }
    }

    this.records = records;
  }
}
