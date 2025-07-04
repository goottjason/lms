package com.goott5.lms.communitynotice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class NoticePagingResponseDTO<E> {

  private final List<E> dtoList; // 일반글 목록 (페이징)
  private final List<E> pinnedList; // 고정글 목록
  private final int totalCount;
  private final NoticePagingRequestDTO pagingRequestDTO;

  private final int pageNo;
  private final int pagingSize;
  private int startPage;
  private int endPage;
  private final int lastPage;
  private final boolean hasPrev;
  private final boolean hasNext;

  @Builder
  public NoticePagingResponseDTO(NoticePagingRequestDTO pagingRequestDTO, List<E> dtoList, int totalCount, List<E> pinnedList) {
    this.pagingRequestDTO = pagingRequestDTO;
    this.dtoList = dtoList;
    this.totalCount = totalCount;
    this.pinnedList = pinnedList;

    this.pageNo = pagingRequestDTO.getPageNo();
    this.pagingSize = pagingRequestDTO.getPagingSize();
    this.lastPage = (int) (Math.ceil((double) totalCount / pagingSize));
    this.endPage = (int) (Math.ceil(this.pageNo / 10.0)) * 10;
    this.startPage = this.endPage - 9;
    this.endPage = Math.min(endPage, lastPage);
    if (this.lastPage == 0) {
      this.endPage = 1;
    }
    this.hasPrev = this.startPage > 1;
    this.hasNext = totalCount > this.endPage * this.pagingSize;
  }
}