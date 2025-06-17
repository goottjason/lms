package com.goott5.lms.courseboardmaterials.service;

import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;
import com.goott5.lms.courseboardmaterials.mapper.CourseBoardMaterialsMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseBoardMaterialsServiceImpl implements CourseBoardMaterialsService {

  private final CourseBoardMaterialsMapper courseBoardMaterialsMapper;

  @Override
  public List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsList() {
    return courseBoardMaterialsMapper.selectAllCourseBoardMaterialsList();
  }

  @Override
  public List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByWriterId(CourseBoardMaterialsDTO dto) {
    return courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByWriterId(dto);
  }

  @Override
  public List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByTitle(CourseBoardMaterialsDTO dto) {
    return courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByTitle(dto);
  }

  @Override
  public List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByContent(CourseBoardMaterialsDTO dto) {
    return courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByContent(dto);
  }

  @Override
  public void insertCourseBoardMaterials(CourseBoardMaterialsDTO courseBoardMaterialsDTO) {
    courseBoardMaterialsMapper.insertCourseBoardMaterials(courseBoardMaterialsDTO);
  }

  @Override
  public CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> getListWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO) {

    List<CourseBoardMaterialsVO> voList = courseBoardMaterialsMapper.selectListWithSearch(
        courseBoardMaterialsPagingRequestDTO);

    for (CourseBoardMaterialsVO vo : voList) {
      log.info("vo = {}", vo);
    }

    List<CourseBoardMaterialsPageDTO> dtoList = new ArrayList<>();

    for (CourseBoardMaterialsVO vo : voList) {
      CourseBoardMaterialsPageDTO dto = CourseBoardMaterialsPageDTO.builder()
          .id(vo.getId())
          .title(vo.getTitle())
          .content(vo.getContent())
          .courseName(vo.getCourseName())
          .writerName(vo.getWriterName())
          .readCount(vo.getReadCount())
          .isFixed(vo.getIsFixed())
          .createdAt(vo.getCreatedAt())
          .updatedAt(vo.getUpdatedAt())
          .deletedAt(vo.getDeletedAt())
          .isAttached(vo.getIsAttached())
          .build();

      dtoList.add(dto);
    }

    int totalCount =courseBoardMaterialsMapper.selectTotalCountWithSearch(
        courseBoardMaterialsPagingRequestDTO);

    return CourseBoardMaterialsPagingResponseDTO.<CourseBoardMaterialsPageDTO>allInfo()
        .courseBoardMaterialsPagingRequestDTO(courseBoardMaterialsPagingRequestDTO)
        .dtoList(dtoList)
        .total(totalCount)
        .build();
  }

  @Override
  @Transactional
  public CourseBoardMaterialsDetailInfo getCourseBoardMaterialsDetail(int id) {
    // 1. 조회수 증가
    log.info("조회수 증가 ID : {}", id);
    courseBoardMaterialsMapper.incrementReadCount(id);

    // 2. 게시글 상세 정보 조회
    log.info("게시글 상세 정보 조회 ID: {}", id);
    CourseBoardMaterialsDetailInfo detail = courseBoardMaterialsMapper.selectCourseBoardMaterialsDetail(id);

    // 3. 조회된 상세 정보 반환
    if (detail == null) {
      log.warn("상세 정보를 찾을수 없습니다. {} ", id);
    } else {
      log.info("상세 정보를 가져옴 : {}", detail);
    }
    return detail;
  }


}
