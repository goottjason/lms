package com.goott5.lms.test;

import com.goott5.lms.test.domain.learnermain.TestHwScheduleVO;
import com.goott5.lms.test.mapper.learnermain.LearnerMainMapper;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class LearnerMainPageTest {

  @Autowired
  public LearnerMainMapper learnerMainMapper;

//  @Test
//  void getTestHwData() {
//
//    int courseId = 37;
//    int userId = 41;
//
//    Integer testAvgScore = learnerMainMapper.selectMyTestAvgScore(userId);
//    Map<String, Object> hwData = learnerMainMapper.selectMyHwData(courseId, userId);
//
//    log.info("testAvgScore={}", testAvgScore);
//    log.info("hwData={}", hwData);
//  }

//  @Test
//  void getTestsHwSchedule() {
//
//    int courseId = 37;
//    int userId = 41;
//
//    List<TestHwScheduleVO> stringObjectMap = learnerMainMapper.selectHwTestSchedule(courseId,
//        userId);
//    log.info("stringObjectMap={}", stringObjectMap);
//  }


}
