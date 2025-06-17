package com.goott5.lms.courseboardmaterials.service;


import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;
import java.util.List;

public interface CourseBoardMaterialsService {

  // 전체 글 조회
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsList();

  // 작성자로 조회
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByWriterId(CourseBoardMaterialsDTO dto);

  // 제목으로 조회
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByTitle(CourseBoardMaterialsDTO dto);

  // 내용으로 조회
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByContent(CourseBoardMaterialsDTO dto);

  // 글 작성
  void insertCourseBoardMaterials(CourseBoardMaterialsDTO  courseBoardMaterialsDTO);

  // 목록 , 페이지네이션 , 검색
  CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> getListWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 게시글 상세 조회
  CourseBoardMaterialsDetailInfo getCourseBoardMaterialsDetail(int id);
}
