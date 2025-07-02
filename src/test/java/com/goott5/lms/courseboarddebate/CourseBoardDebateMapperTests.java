package com.goott5.lms.courseboarddebate;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDTO;
import com.goott5.lms.courseboarddebate.mapper.CourseBoardDebateMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Slf4j
public class CourseBoardDebateMapperTests {

  @Autowired
  private CourseBoardDebateMapper courseBoardDebateMapper;

  @Test
  @Rollback(value = false)
  public void insertDummyData(){

    for(int i = 0; i < 101; i++){
      CourseBoardDebateDTO dto = CourseBoardDebateDTO.builder()
          .courseId(38)
          .writerId(33)
          .title("더미 데이터 "+ i)
          .content("더미 데이터 " +i)
          .build();

      courseBoardDebateMapper.insertCourseBoardDebate(dto);
    }
  }

}
