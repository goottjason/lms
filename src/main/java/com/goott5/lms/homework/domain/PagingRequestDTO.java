package com.goott5.lms.homework.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PagingRequestDTO {

  @Builder.Default
  @Min(value = 1)
  private int pageNo = 1; // 현 페이지

  @Builder.Default
  @Min(value = 3)
  @Max(value = 5)
  private int pageSize = 5;  //한 페이지당 보여줄 게시글

  private String link; // 추후 해당 링크로 다시 이동?

  private String keyword; // 검색 키워드
//  private String type; //검색 타입 : c, t, w, tc, tcw

  public int getSkip() {
    return (pageNo - 1) * pageSize;
  }



  public int getLimit() {
    return pageSize;
  }

  public String getLink() {
    if (link == null) {
      link = generateLink();
    }

    return link;
  }

  private String generateLink() {

    StringBuilder sb = new StringBuilder();

    sb.append("pageNo=").append(pageNo)
        .append("&pageSize=").append(pageSize);

    if (keyword != null && !keyword.isBlank()) {
      sb.append("&keyword=").append(keyword);
    }

    return sb.toString();

  }

  public String generateLinkExceptionPageNo() {

    StringBuilder sb = new StringBuilder();

    sb.append("pageSize=").append(pageSize);

    if (keyword != null && !keyword.isBlank()) {
      sb.append("&keyword=").append(keyword);
    }

    return sb.toString();
  }




}
