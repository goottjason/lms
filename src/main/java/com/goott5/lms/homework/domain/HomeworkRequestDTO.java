package com.goott5.lms.homework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HomeworkRequestDTO {

  // 강사,학생만-사용자
  private String loginId;

  // 진행 여부(학생,강사)
  //private Boolean isInProgressForLt;

  // 진행 여부 (관리자용)
  private Boolean isInProgressForAdmin;

  // 강사/학생용
  private String nameForLt;

  // 관리자용
  private String nameForAdmin;

  //검색용
  private String keyword;

  // 공통
  private String title;

  // 정렬
  private String sortBy;            // "endDate" or "title"
  private String order;             // "asc" or "desc"

  // (추후 페이징 추가 시)
  //private Integer pageNo;
  //private Integer pageSize;

  @Builder.Default
  private PagingRequestDTO pagingRequest = PagingRequestDTO.builder()
      .pageNo(1)
      .pageNo(5)
      .build();

}
