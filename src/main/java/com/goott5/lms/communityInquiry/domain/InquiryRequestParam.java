package com.goott5.lms.communityInquiry.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class InquiryRequestParam {

  @Builder.Default
  private int id = -1;

  private int userId;
  private String userType;

  @Builder.Default
  @Min(value = 1)
  @Positive
  private int pageNo = 1;

  @Builder.Default
  @Positive
  private int pageSize = 10;

  @Builder.Default
  private String searchType = "";

  @Builder.Default
  private String keyword = "";

  @Builder.Default
  private String orderBy = "desc";

  @Builder.Default
  private String sort = "createdAt";

  public int getSkip() {
    return (pageNo - 1) * pageSize;
  }

  public String getQueryString() {
    StringBuilder queryString = new StringBuilder();
    queryString.append("pageNo=").append(pageNo);
    queryString.append("&searchType=").append(searchType);
    queryString.append("&keyword=").append(keyword);
    queryString.append("&orderBy=").append(orderBy);
    queryString.append("&sort=").append(sort);

    return queryString.toString();
  }

}
