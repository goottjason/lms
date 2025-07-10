package com.goott5.lms.test.controller;

import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.test.domain.learnermain.AttendanceStatusVO;
import com.goott5.lms.test.domain.learnermain.CourseScheduleVO;
import com.goott5.lms.test.domain.learnermain.ForumVO;
import com.goott5.lms.test.domain.learnermain.InquiryVO;
import com.goott5.lms.test.domain.learnermain.NoticeVO;
import com.goott5.lms.test.domain.learnermain.QnAVO;
import com.goott5.lms.test.domain.learnermain.TestHwScheduleVO;
import com.goott5.lms.test.domain.learnermain.TestVO;
import com.goott5.lms.test.service.learnermain.LearnerMainService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class LearnerMainPageController {

  private final LearnerMainService learnerMainService;

  @GetMapping("/learner/attendance/progress")
  public ResponseEntity<ApiResult<Map<String, Object>>> getAttendanceRateAndCourseProgress(
      String courseName, HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
        learnerMainService.getAttendanceRateAndCourseProgress(courseName, session));
  }

  @GetMapping("/learner/test/hw")
  public ResponseEntity<ApiResult<Map<String, Object>>> getTestHomeworkData(String courseName,
      HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
        learnerMainService.getTestHomeworkData(courseName, session));
  }

  @GetMapping("/learner/course/schedule")
  public ResponseEntity<ApiResult<List<CourseScheduleVO>>> getCourseSchedule(
      @RequestParam(name = "courseName", required = true) String courseName) {

    return ApiResult.respondOk(200, "SUCCESS", learnerMainService.getCourseSchedule(courseName));
  }

  @GetMapping("/learner/test/hw/schedule")
  public ResponseEntity<ApiResult<List<TestHwScheduleVO>>> getTestHwSchedule(String courseName,
      HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
        learnerMainService.getTestHwSchedule(courseName, session));
  }

  @GetMapping("/learner/attendance/status")
  public ResponseEntity<ApiResult<List<AttendanceStatusVO>>> getAttendanceStatus(
      @RequestParam(name = "courseName", required = true) String courseName,
      HttpSession session) {

//    log.info("courseName={}", courseName);
    return ApiResult.respondOk(200, "SUCCESS",
        learnerMainService.getAttendanceStatus(courseName, session));
  }

  @GetMapping("/learner/inquiry")
  public ResponseEntity<ApiResult<List<InquiryVO>>> getInquiry(HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS", learnerMainService.getInquiry(session));
  }

  @GetMapping("/learner/qna")
  public ResponseEntity<ApiResult<List<QnAVO>>> getQnA(HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS", learnerMainService.getQnA(session));
  }

  @GetMapping("/learner/notice")
  public ResponseEntity<ApiResult<List<NoticeVO>>> getNotice(
      @RequestParam(name = "courseName", required = true) String courseName) {

    return ApiResult.respondOk(200, "SUCCESS", learnerMainService.getNotice(courseName));
  }

  @GetMapping("/learner/forum")
  public ResponseEntity<ApiResult<List<ForumVO>>> getForum(
      @RequestParam(name = "courseName", required = true) String courseName) {

    return ApiResult.respondOk(200, "SUCCESS", learnerMainService.getForum(courseName));
  }

  @GetMapping("/learner/test/statistic")
  public ResponseEntity<ApiResult<List<TestVO>>> getTestStatistic(
      @RequestParam(name = "courseName", required = true) String courseName, HttpSession session) {

    return ApiResult.respondOk(200, "SUCCESS",
        learnerMainService.getTestStatistic(courseName, session));
  }
}
