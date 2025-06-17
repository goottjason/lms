package com.goott5.lms.homework.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
public class PagingResponseDTO<T> {

  private int pageNo; // 현재 페이지
  private int pageSize; //한 페이지당 게시글 수
  private int total; //전체 게시글 수?

  private int start; // 한 블럭 당 시작 페이지
  private int end; // 한 블럭 당 끝 페이지
  private int last; // 가장 마지막 페이지

  private boolean prev; // 이전 블럭으로 이동
  private boolean next; //이후 블럭으로 이동

  private List<T> dtoList; // 출력할 dto(homework...리스트)

  @Builder(builderMethodName = "allInfo") //빌더 이름 지정?
  private PagingResponseDTO(PagingRequestDTO pagingRequestDTO, List<T> dtoList, int total) {

    this.pageNo = pagingRequestDTO.getPageNo();
    this.pageSize = pagingRequestDTO.getPageSize();

    this.total = total;
    this.dtoList = dtoList;

    this.end = (int) (Math.ceil(this.pageNo / 10.0)) * 10;
    this.start = this.end - 9;
    this.last = (int) (Math.ceil(total / (double) pageSize));

    // end == (end>last)?:last:end
    this.end = this.end > last ? last : end;

    this.prev = this.start > 1;
    this.next = this.end < this.last;

    if (this.end == 0) {
      this.end = 1;
    }

  }


}
