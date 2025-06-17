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
public class PageCourseRespDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<T> respDTOS;

  @Builder(builderMethodName = "withPageInfo")
  public PageCourseRespDTO(PageCourseReqDTO pageCourseReqDTO, List<T> respDTOS, int totalRecords) {

    this.totalRecords = totalRecords;

    this.pageNo = pageCourseReqDTO.getPageNo();
    this.pageSize = pageCourseReqDTO.getPageSize();
    this.respDTOS = respDTOS;

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

  }

}
