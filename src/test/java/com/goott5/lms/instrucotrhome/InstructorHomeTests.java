package com.goott5.lms.instrucotrhome;

import com.goott5.lms.instructorhome.mapper.InstructorHomeMapper;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class InstructorHomeTests {

  @Autowired
  private InstructorHomeMapper instructorHomeMapper;

  @Test
  public void test() {

    log.info("count : {}", instructorHomeMapper.selectCountOfCompletedDays(37, LocalDate.now()));
  }


}
