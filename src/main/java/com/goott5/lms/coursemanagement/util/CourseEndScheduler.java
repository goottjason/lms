package com.goott5.lms.coursemanagement.util;

import com.goott5.lms.coursemanagement.domain.dto.PageCourseRequest;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseResponse;
import com.goott5.lms.coursemanagement.domain.integrated.CourseOverviewResp;
import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.learnermanagement.service.LearnerManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.service.OperationsManagementService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEndScheduler {
  private final CourseManagementService courseManagementService;
  private final SchedulerStatusService schedulerStatusService;

  @Scheduled(cron = "0 55 23 * * MON-FRI", zone = "Asia/Seoul")
  public void endCourseAutoProcess() {

    courseManagementService.endCoursesAutoProcess();
    schedulerStatusService.updateLastExecution();
  }

}

