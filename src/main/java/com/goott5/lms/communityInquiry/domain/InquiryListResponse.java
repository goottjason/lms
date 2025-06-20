package com.goott5.lms.communityInquiry.domain;

import java.util.List;
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
public class InquiryListResponse {

  private List<InquiryVO> inquiryList;

  private int boardTotalCount;
  private int lastPage;
  private int startPage;
  private int endPage;
  private boolean prev;
  private boolean next;

}
