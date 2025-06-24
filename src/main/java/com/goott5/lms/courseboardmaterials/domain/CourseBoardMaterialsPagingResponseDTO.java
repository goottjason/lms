package com.goott5.lms.courseboardmaterials.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class CourseBoardMaterialsPagingResponseDTO<T> {

  private int pageNo;
  private int pagingSize;
  private int total;

  private int start; // 시작 페이지 번호
  private int end; // 끝 페이지 번호
  private int last; // 마지막 페이지

  private boolean prev; // 이전페이지 존재 여부
  private boolean next; // 다음페이지 존재 여부

  private List<T> dtoList;

  private CourseBoardMaterialsPagingRequestDTO pagingRequestDTO;
  private List<T> pinnedList;


  @Builder(builderMethodName = "allInfo") // 빌더의 이름을 지정
  public CourseBoardMaterialsPagingResponseDTO(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO,List<T> dtoList, int total,List<T> pinnedList) {
    this.pagingRequestDTO = courseBoardMaterialsPagingRequestDTO;
    this.pageNo = courseBoardMaterialsPagingRequestDTO.getPageNo();
    this.pagingSize = courseBoardMaterialsPagingRequestDTO.getPagingSize();

    this.total= total;
    this.dtoList = dtoList;
    this.pinnedList = pinnedList;

    this.end = (int)(Math.ceil(pageNo / 10.0)) * 10;
//        this.end = (((10 - 1) / 10 ) 몫 + 1) * 10 // ok
    this.start = this.end - 9;
    this.last = (int)(Math.ceil(total / (double)pagingSize));

    // 페이징 블럭의 end 페이지가 마지막 페이지보다 크면 last값이 end가 되어야 한다.
    this.end = end > last ? last : end;

    this.prev = this.start > 1;

    this.next = this.end < this.last;

    if (this.end == 0) {
      this.end = 1;
    }
  }
}
