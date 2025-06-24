package com.goott5.lms.courseboardmaterials.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CourseBoardMaterialsPagingRequestDTO {

  @Builder.Default
  @Min(value = 1)
//    @Positive
  private int pageNo = 1;

  @Builder.Default
  @Min(value = 10)
  @Max(value = 100)
//    @Positive
  private int pagingSize = 10;

  private String link;
  private Integer courseId;
  private String courseName;
  private String keyword;
  private String type; // 검색타입 : c, t, w, tc, tcw

  public int getSkip () {
    return (pageNo - 1) * pagingSize;
  }

  public String getLink(){
    if (link == null) {
      link = generateLink();
    }
    return link;
  }

  private Boolean hasAttachment; // 첨부파일 여부 (true:있음, false:없음, null:전체)
  private String orderBy;        // 정렬 기준 (예: "createdAt", "readCount", "title")
  private String orderDirection; // 정렬 방향 (예: "desc", "asc")
  private Boolean isInProgress;

  private String generateLink() {

    StringBuilder sb = new StringBuilder();

    sb.append("pageNo=").append(pageNo)
        .append("&pagingSize=").append(pagingSize);

    if (courseId != null && courseId > 0) {
      sb.append("&courseId=").append(courseId);
    }

    if (courseName != null && courseName.isBlank()) {
      sb.append("&courseName=").append(courseName);
    }

    if (isInProgress != null ) {
      sb.append("&isInProgress=").append(isInProgress);
    }

    if (type != null && !type.isBlank()) {
      sb.append("&type=").append(type);
    }

    if (keyword != null && !keyword.isBlank()) {
      sb.append("&keyword=").append(keyword);
    }

    if (hasAttachment != null) {
      sb.append("&hasAttachment=").append(hasAttachment);
    }

    if (orderBy != null && !orderBy.isBlank()) {
      sb.append("&orderBy=").append(orderBy);
    }

    if (orderDirection != null && !orderDirection.isBlank()) {
      sb.append("&orderDirection=").append(orderDirection);
    }

    return sb.toString();
  }

  public List<String> getSearchTypes() {
    if (type == null || type.isEmpty()) {
      return null;
    }

    return Arrays.asList(type.split("")); // String[] -> List<String>

  }

  public String generateLinkExceptPageNo() {

    StringBuilder sb = new StringBuilder();

    sb.append("&pagingSize=").append(pagingSize);

    if (courseId != null && courseId > 0) {
      sb.append("&courseId=").append(courseId);
    }

    if (courseName != null && courseName.isBlank()) {
      sb.append("&courseName=").append(courseName);
    }

    if (isInProgress != null) {
      sb.append("&isInProgress=").append(isInProgress);
    }

    if (type != null && !type.isBlank()) {
      sb.append("&type=").append(type);
    }

    if (keyword != null && !keyword.isBlank()) {
      sb.append("&keyword=").append(keyword);
    }

    if (hasAttachment != null) {
      sb.append("&hasAttachment=").append(hasAttachment);
    }

    if (orderBy != null && !orderBy.isBlank()) {
      sb.append("&orderBy=").append(orderBy);
    }

    if (orderDirection != null && !orderDirection.isBlank()) {
      sb.append("&orderDirection=").append(orderDirection);
    }

    return sb.toString();
  }

}
