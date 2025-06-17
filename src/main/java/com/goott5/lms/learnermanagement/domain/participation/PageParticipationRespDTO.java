package com.goott5.lms.learnermanagement.domain.participation;

import com.goott5.lms.learnermanagement.domain.PageLearnerReqDTO;
import lombok.*;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageParticipationRespDTO<T> {

  private Integer pageNo;
  private Integer pageSize;

  private Integer totalRecords;

  private Integer blockStartPage;
  private Integer blockEndPage;
  private Integer lastPage;

  private List<T> respDTOS;

  private HashMap<String, Integer> statusCountMap;
  private Double attendanceRate;

  @Builder(builderMethodName = "withPageInfo")
  public PageParticipationRespDTO(PageParticipationReqDTO pageParticipationReqDTO, List<T> respDTOS, int totalRecords, HashMap<String, Integer> statusCountMap, Double attendanceRate) {

    this.totalRecords = totalRecords;

    this.pageNo = pageParticipationReqDTO.getPageNo();
    this.pageSize = pageParticipationReqDTO.getPageSize();

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
    this.statusCountMap = statusCountMap;
    this.attendanceRate = attendanceRate;
  }

}
