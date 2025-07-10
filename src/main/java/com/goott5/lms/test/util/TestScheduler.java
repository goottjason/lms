package com.goott5.lms.test.util;

import com.goott5.lms.test.domain.test.register.vo.TestRegisterVO;
import com.goott5.lms.test.mapper.test.TestRegisterMapper;
import com.goott5.lms.test.service.test.submission.TestSubmissionService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestScheduler implements ApplicationRunner {

  private final TaskScheduler taskScheduler;
  private final TestSubmissionService testSubmissionService;
  private final TestRegisterMapper testRegisterMapper;

  @Override
  public void run(ApplicationArguments args) {
    LocalDateTime now = LocalDateTime.now();

    // auto_graded = false 인 모든 테스트 조회
    List<TestRegisterVO> pending = testRegisterMapper.findPendingAutoGradeTests();

    for (TestRegisterVO t : pending) {
      LocalDateTime end = t.getEndDate();
      int testId = t.getId();

      if (!end.isAfter(now)) {
        // 이미 종료된 시험 → 즉시 0점 처리
//        log.info("Auto-grading immediately for past test id={}", testId);
        testSubmissionService.assignZeroToNoShows(testId);
      } else {
        // 아직 종료되지 않은 시험 → 종료 시각에 예약
//        log.info("Scheduling auto-grading for future test id={} at {}", testId, end);
        scheduleAutoGrading(testId, end);
      }
    }
  }

  // 주어진 종료 시각(endTime)에 한 번만 호출되어, 미응시자(노쇼)에게 0점 처리 로직을 실행하도록 스케줄링
  public void scheduleAutoGrading(int testId, LocalDateTime endTime) {
    Date runDate = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());

    taskScheduler.schedule(
        () -> testSubmissionService.assignZeroToNoShows(testId),
        runDate
    );
  }
}
