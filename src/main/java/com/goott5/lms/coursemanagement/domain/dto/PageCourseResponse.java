package com.goott5.lms.coursemanagement.domain.dto;
import com.goott5.lms.learnermanagement.domain.dto.LearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerRequest;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
