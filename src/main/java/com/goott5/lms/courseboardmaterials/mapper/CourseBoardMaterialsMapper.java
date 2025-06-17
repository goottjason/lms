package com.goott5.lms.courseboardmaterials.mapper;

import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface CourseBoardMaterialsMapper {

  @Select("select now()")
  String selectNow();

  //  전체 리스트를 불러오는 쿼리문
  @Select("select * from course_notice where deleted_at is null")
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsList();

  // 작성자 리스트를 불러오는 쿼리문
  @Select("select * from course_notice where writer_id = #{writerId}")
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByWriterId(CourseBoardMaterialsDTO dto);

  // 제목 리스트를 불러오는 쿼리문
  @Select("select * from course_notice where title = #{title}")
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByTitle(CourseBoardMaterialsDTO dto);

  // 내용 리스트를 불러오는 쿼리문
  @Select("select * from course_notice where content = #{content}")
  List<CourseBoardMaterialsVO> selectAllCourseBoardMaterialsListByContent(CourseBoardMaterialsDTO dto);

  // 공지 작성 쿼리문
  @Insert("insert into course_notice(course_id, writer_id, title, content) values(#{courseId}, #{writerId}, #{title},#{content})")
  void insertCourseBoardMaterials(CourseBoardMaterialsDTO  courseBoardMaterialsDTO);

  // 검색 기능
  List<CourseBoardMaterialsVO> selectListWithSearch(CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 검색된 총 글의 개수
  int selectTotalCountWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO);

  // 게시글 상세 조회 (작성자 타입 포함)
  CourseBoardMaterialsDetailInfo selectCourseBoardMaterialsDetail(@Param("id") int id);

  // 조회수 증가
  void incrementReadCount(@Param("id") int id);
}
