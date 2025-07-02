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
  private final LearnerManagementService learnerManagementService;
  private final OperationsManagementService operationsManagementService;
  private final SchedulerStatusService schedulerStatusService;

  @Scheduled(cron = "0 38 13 * * MON-FRI", zone = "Asia/Seoul")
  public void endCourseAutoProcess() {

    LocalDate today = LocalDate.now();

    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(33)
        .loginUserType("ADMINISTRATOR")
        .loginUserPosition("GENERAL_MANAGER")
        .build();
    PageCourseRequest pageCourseRequest = PageCourseRequest.builder().build();

    PageCourseResponse<CourseOverviewResp> coursesWithPagination =
        courseManagementService.getCoursesByAuth(baseReqDTO, pageCourseRequest);
    coursesWithPagination.getRecords().forEach(course -> {
      // 오늘을 포함하여 이미 종료된 과정 조회
      if(today.isAfter(course.getCourseWithAssignedInfo().getCoEndDate())) {
        if (course.getCourseWithAssignedInfo().getCoIsInProgress()) {
          log.info("오늘을 포함하여 이미 종료된 과정: {} (종료일: {})",
              course.getCourseWithAssignedInfo().getCoName(),
              course.getCourseWithAssignedInfo().getCoEndDate());

          CourseWithAssignedInfo info = course.getCourseWithAssignedInfo();
          // 오늘 종료된 과정은 종료처리 (혹시라도 종료되지 못한 과정 또한 종료처리)

          // 과정 상태 업데이트
          Boolean result1 = courseManagementService.modifyCourseIsInProgressByCoId(info.getCoId());
          log.info("과정상태 업데이트 완료");

          // 강의실 비활성화
          Boolean result2 = operationsManagementService.modifyClassroomIsActiveBycoClassroomId(info.getCoClassroomId());
          log.info("강의실 비활성화 완료");

          // 교육생 수료처리
          Boolean result3 = learnerManagementService.modifyCompletionStatusByCoId(baseReqDTO, info.getCoId());
          log.info("교육생 수료 또는 중도탈퇴 처리 완료");
        }
      }
    });

    schedulerStatusService.updateLastExecution();

  }

}

