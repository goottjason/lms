package com.goott5.lms.test.service.learnermain;

import com.goott5.lms.test.domain.learnermain.AttendanceStatusVO;
import com.goott5.lms.test.domain.learnermain.CourseScheduleVO;
import com.goott5.lms.test.domain.learnermain.ForumVO;
import com.goott5.lms.test.domain.learnermain.InquiryVO;
import com.goott5.lms.test.domain.learnermain.NoticeVO;
import com.goott5.lms.test.domain.learnermain.QnAVO;
import com.goott5.lms.test.domain.learnermain.TestHwScheduleVO;
import com.goott5.lms.test.domain.learnermain.TestVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface LearnerMainService {


  // 출결률 + 과정 진행률 조회
  Map<String, Object> getAttendanceRateAndCourseProgress(String courseName, HttpSession session);

  // 시험 + 과제 data
  Map<String, Object> getTestHomeworkData(String courseName, HttpSession session);

  // 과정 일정 data
  List<CourseScheduleVO> getCourseSchedule(String courseName);

  // 시험 + 과제 일정 data
  List<TestHwScheduleVO> getTestHwSchedule(String courseName, HttpSession session);

  // 이번주 출석 data
  List<AttendanceStatusVO> getAttendanceStatus(String courseName, HttpSession session);

  // 1대1 문의 data
  List<InquiryVO> getInquiry(HttpSession session);

  // qna data
  List<QnAVO> getQnA(HttpSession session);

  // 과정 공지 data
  List<NoticeVO> getNotice(String courseName);

  // 인기글 data
  List<ForumVO> getForum(String courseName);

  // 시험 통계 data
  List<TestVO> getTestStatistic(String courseName, HttpSession session);
}
