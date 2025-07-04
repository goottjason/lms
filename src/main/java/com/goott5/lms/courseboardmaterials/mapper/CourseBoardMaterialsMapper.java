package com.goott5.lms.courseboardmaterials.mapper;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsFlatDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface CourseBoardMaterialsMapper {

  // 게시글 등록
  int insertCourseBoardMaterials(@Param("dto")CourseBoardMaterialsDTO  courseBoardMaterialsDTO);

  // 검색 기능
  List<CourseBoardMaterialsVO> selectListWithSearch(CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 검색된 총 글의 개수
  int selectTotalCountWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 게시글 상세 조회 (작성자 타입 포함)
  CourseBoardMaterialsDetailInfo selectCourseBoardMaterialsDetail(@Param("id") int id);

  // 조회수 증가
  int updateReadCount(@Param("id") int id);

  // 게시글+작성자 정보 조회용
  CourseBoardMaterialsFlatDTO selectCourseBoardMaterialsDetailFlat(@Param("id") int id);

  // 첨부파일 목록 조회용
  List<FileSelectDTO> selectAttachmentsByBoardId(@Param("id") int id);

  // 게시글 수정
  int updateCourseBoardMaterials(CourseBoardMaterialsDTO courseBoardMaterialsDTO);

  // 게시글 삭제
  int softDeleteById(@Param("id") int id);

  // 고정 기능
  int countFixedPosts(@Param("courseId") Long courseId);

  List<CourseBoardMaterialsPageDTO> selectAllFixedPosts(CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 과정 ID(PK) 가져오기
  @Select("SELECT id FROM course WHERE name = #{courseName} ")
  Integer selectCourseId(String courseName);


}