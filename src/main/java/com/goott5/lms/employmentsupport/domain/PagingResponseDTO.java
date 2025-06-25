package com.goott5.lms.employmentsupport.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PagingResponseDTO<T> {

  private int pageNo;
  private int pagingSize;
  private int total;

  private int start; // (페이지 블럭에서의) 시작 페이지 번호
  private int end; // (페이지 블럭에서의) 끝 페이지 번호
  private int last; // 마지막 페이지

  private boolean prev; // 이전페이지 존재 여부
  private boolean next; // 다음페이지 존재 여부

  private List<T> voList;

  @Builder(builderMethodName = "allInfo") // 빌더 이름 지정
  public PagingResponseDTO(PagingRequestDTO pagingRequestDTO, List<T> voList, int total) {
    pageNo = pagingRequestDTO.getPageNo();
    pagingSize = pagingRequestDTO.getPagingSize();
    this.total = total;
    this.voList = voList;

    end = (((pageNo - 1) / 10) + 1) * 10;
    start = end - 9;
    last = (int) (Math.ceil(total / (double) pagingSize));
    end = end > last ? last : end;

    if (this.total == 0) {
      start = 1;
      end = 1;
      last = 1;
    }

    prev = start > 1;
    next = end < last;


  }
}
