package com.goott5.lms.courseboardmaterials.mapper;

import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Slf4j
public class CourseBoardMaterialsMapperTests {

  @Autowired
  private CourseBoardMaterialsMapper courseBoardMaterialsMapper;

  @Test
  public void testsSelectAllCourseBoardMaterialsList() {

    List<CourseBoardMaterialsVO> list = courseBoardMaterialsMapper.selectAllCourseBoardMaterialsList();

    log.info("전체 글={}", list);
  }

  @Test
  public void testsSelectAllCourseBoardMaterialsById() {

    CourseBoardMaterialsDTO dto = CourseBoardMaterialsDTO.builder()
        .writerId(5)
        .build();

    List<CourseBoardMaterialsVO> list = courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByWriterId(dto);
    log.info("작성자만 조회={}", list);
  }

  @Test
  public void testsSelectAllCourseBoardMaterialsByTitle() {

    CourseBoardMaterialsDTO dto = CourseBoardMaterialsDTO.builder()
        .title("공지")
        .build();

    List<CourseBoardMaterialsVO> list = courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByTitle(dto);
    log.info("제목만 조회={}", list);
  }

  @Test
  public void testsSelectAllCourseBoardMaterialsByContent() {

    CourseBoardMaterialsDTO dto = CourseBoardMaterialsDTO.builder()
        .content("test")
        .build();

    List<CourseBoardMaterialsVO> list = courseBoardMaterialsMapper.selectAllCourseBoardMaterialsListByContent(dto);
    log.info("list={}", list);
  }

  @Test
  public void testsInsertCourseBoardMaterials() {

    CourseBoardMaterialsDTO dto =  CourseBoardMaterialsDTO.builder()
        .courseId(2)
        .writerId(2)
        .title("test1")
        .content("test1")
        .build();

    log.info("글 작성={}", dto);

    int result;
    courseBoardMaterialsMapper.insertCourseBoardMaterials(dto);
    int id = dto.getCourseId();
    log.info("insert result={}", id);

  }

  @Test
  @Rollback(value = false)
  public void insertDummyData(){

    for(int i = 0; i < 500; i++){
      CourseBoardMaterialsDTO dto = CourseBoardMaterialsDTO.builder()
          .courseId(1)
          .writerId(5)
          .title("dummy "+ i)
          .content("dummy " +i)
          .build();

      courseBoardMaterialsMapper.insertCourseBoardMaterials(dto);
    }
  }

  @Test
  public void testsSelectCourseBoardMaterialsDetail() {

    int id = 1;

    CourseBoardMaterialsDetailInfo detail = courseBoardMaterialsMapper.selectCourseBoardMaterialsDetail(id);
    log.info("<UNK> <UNK>={}", detail);

  }
}
