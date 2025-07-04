package com.goott5.lms.communitynotice.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.web.util.UriComponentsBuilder;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NoticePagingRequestDTO {

  @Builder.Default
  @Min(value = 1)
  private int pageNo = 1; // 현재 페이지 번호

  @Builder.Default
  @Min(value = 10)
  @Max(value = 100)
  private int pagingSize = 10; // 페이지 당 게시글 수

  @Builder.Default
  private String searchType = "title"; // 검색 타입 (title, content, title_content)
  private String keyword; // 검색어

  @Builder.Default
  private String sortProperty = "createdAt"; // 정렬 기준 (createdAt, readCount)
  @Builder.Default
  private String sortOrder = "DESC"; // 정렬 순서 (ASC, DESC)


  public int getSkip() {
    return (pageNo - 1) * pagingSize;
  }

  public String getLink() {
    return UriComponentsBuilder.fromPath("")
        .queryParam("pageNo", this.pageNo)
        .queryParam("pagingSize", this.pagingSize)
        .queryParam("searchType", this.searchType)
        .queryParam("keyword", this.keyword)
        .queryParam("sortProperty", this.sortProperty)
        .queryParam("sortOrder", this.sortOrder)
        .toUriString();
  }
}