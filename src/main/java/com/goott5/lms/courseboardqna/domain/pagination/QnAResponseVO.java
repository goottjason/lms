package com.goott5.lms.courseboardqna.domain.pagination;

import com.goott5.lms.test.domain.pagination.RequestVO;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QnAResponseVO<T> {

  private int currentPageNo; // 현재 선택된 페이지
  private int totalItemsCount; // 총 리스트 수z
  private int pageSize; // 한 페이지에 보여줄 아이템 수

  private int startPageNo; // 시작 페이지 번호
  private int endPageNo; // 끝 페이지 번호
  private int totalPages; // 총 페이지 수

  private int currentPageGroup; // 현재 선택된 페이지의 그룹
  private int totalPageGroup; // 총 페이지 그룹 수
  private int pagesPerGroup; // 한 페이지 그룹의 페이지 버튼 수

  private boolean prev;
  private boolean next;

  private List<T> items;

  @Builder(builderMethodName = "allInfo")
  public QnAResponseVO(QnARequestVO qnaRequestVO, List<T> items) {

    System.out.println("🔥 생성자 실행됨! qnaRequestVO=" + qnaRequestVO);
    this.currentPageNo = qnaRequestVO.getCurrentPageNo(); // 현재 선택된 페이지 번호

    this.pagesPerGroup = qnaRequestVO.getPagesPerGroup(); // 한 페이지 그룹의 페이지 버튼 수
    this.pageSize = qnaRequestVO.getPageSize(); // 한 페이지에 보여줄 아이템 수

    this.totalItemsCount = qnaRequestVO.getTotalItemsCount(); // 총 아이템 수

    this.totalPages = (int) (Math.ceil(this.totalItemsCount / (double) this.pageSize));
    this.currentPageGroup = (int) Math.ceil(this.currentPageNo / (double) this.pagesPerGroup);
    this.totalPageGroup = (int) Math.ceil(this.totalPages / (double) this.pagesPerGroup);

    this.startPageNo = (this.currentPageGroup - 1) * this.pagesPerGroup + 1;
    this.endPageNo = Math.min(this.totalPages, this.currentPageGroup * this.pagesPerGroup);

    this.prev = this.currentPageGroup > 1;
    this.next = this.currentPageGroup < this.totalPageGroup;

    this.items = items;

    if (qnaRequestVO.getTotalItemsCount() == 0 || this.currentPageNo > this.totalPages) {
      this.currentPageNo = 0;

      this.totalPages = 0;
      this.totalPageGroup = 0;
      this.startPageNo = 0;
      this.endPageNo = 0;
    }
  }

}
