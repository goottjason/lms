package com.goott5.lms.operationsmanagement.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PageStaffHistoryRespDTO<StaffHistoryResp> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<StaffHistoryResp> staffHistories;

  @Builder(builderMethodName = "withPageInfo")
  public PageStaffHistoryRespDTO(
      PageStaffHistoryReqDTO pageStaffHistoryReqDTO,
      List<StaffHistoryReq> staffHistories) {

    this.totalRecords = staffHistories.size();

    this.pageNo = pageStaffHistoryReqDTO.getPageNo();
    this.pageSize = pageStaffHistoryReqDTO.getPageSize();

    if (pageNo != null || pageSize != null) {

      // totalRecords가 0이면 모든 페이지 계산 생략
      if (totalRecords == 0) {
        this.blockEndPage = 0;
        this.blockStartPage = 0;
        this.lastPage = 0;
        return;
      }

      this.lastPage = (int) (Math.ceil(this.totalRecords / (double) pageSize));

      this.blockEndPage = (((this.pageNo - 1) / this.pageSize) + 1) * this.pageSize;
      this.blockStartPage = this.blockEndPage - (this.pageSize - 1);
      this.blockEndPage = Math.min(this.blockEndPage, this.lastPage);
    }

    int endIndex = Math.min(pageStaffHistoryReqDTO.getOffset() + pageSize, totalRecords);
    this.staffHistories = (List<StaffHistoryResp>) staffHistories.subList(pageStaffHistoryReqDTO.getOffset(), endIndex);
  }
}
