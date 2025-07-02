package com.goott5.lms.courseboardmaterials;

import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;

import com.goott5.lms.courseboardmaterials.mapper.CourseBoardMaterialsMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Slf4j
public class CourseBoardMaterialsMapperTests {

  @Autowired
  private CourseBoardMaterialsMapper courseBoardMaterialsMapper;


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

    for(int i = 0; i < 101; i++){
      CourseBoardMaterialsDTO dto = CourseBoardMaterialsDTO.builder()
          .courseId(38)
          .writerId(33)
          .title("더미 데이터입니다 "+ i)
          .content("더미 데이터입니다 " +i)
          .isFixed(false)
          .build();

      courseBoardMaterialsMapper.insertCourseBoardMaterials(dto);
    }
  }

  @Test
  public void testsSelectCourseBoardMaterialsDetail() {

    int id = 1;

    CourseBoardMaterialsDetailInfo detail = courseBoardMaterialsMapper.selectCourseBoardMaterialsDetail(id);
    log.info("글 상세={}", detail);

  }
}
