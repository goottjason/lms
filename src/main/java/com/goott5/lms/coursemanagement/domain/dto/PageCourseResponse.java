package com.goott5.lms.coursemanagement.domain.dto;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageCourseResponse<T> {
  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<T> records;

  @Builder(builderMethodName = "withPageInfo")
  public PageCourseResponse(PageCourseRequest request, List<T> records, Integer totalRecords) {
    this.totalRecords = totalRecords;

    pageNo = request.getPageNo();
    pageSize = request.getPageSize();

    if (pageNo != null && pageSize != null) {

      blockEndPage = (((pageNo - 1) / pageSize) + 1) * pageSize;
      blockStartPage = blockEndPage - (pageSize - 1);
      lastPage = (int) (Math.ceil(totalRecords / (double) pageSize));

      blockEndPage = Math.min(blockEndPage, lastPage);

      if (blockEndPage == 0) {
        blockEndPage = 1;
      }
    }

    this.records = records;
  }
}
