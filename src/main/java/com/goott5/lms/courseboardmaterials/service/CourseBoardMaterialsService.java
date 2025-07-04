package com.goott5.lms.courseboardmaterials.service;


import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;

public interface CourseBoardMaterialsService {

  // 글 작성
  int insertCourseBoardMaterials(CourseBoardMaterialsDTO  courseBoardMaterialsDTO);

  // 목록 , 페이지네이션 , 검색
  CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> getListWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 게시글 상세 조회
  CourseBoardMaterialsDetailInfo getCourseBoardMaterialsDetail(int id);

  // 조회수 처리
  boolean updateReadCount(ReadCountLog readCountLog);

  // 게시글 수정
  int updateCourseBoardMaterials(CourseBoardMaterialsDTO courseBoardMaterialsDTO);

  // 게시글 삭제
  void deleteCourseBoardMaterials(int courseBoardMaterialsId);

  // 고정글 개수
  int countFixedPostsByCourseId(Long courseId);

}
