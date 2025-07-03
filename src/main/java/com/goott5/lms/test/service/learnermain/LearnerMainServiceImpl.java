package com.goott5.lms.test.service.learnermain;

import com.goott5.lms.participation.mapper.ParticipationMapper;
import com.goott5.lms.test.domain.learnermain.AttendanceStatusVO;
import com.goott5.lms.test.domain.learnermain.CourseScheduleVO;
import com.goott5.lms.test.domain.learnermain.ForumVO;
import com.goott5.lms.test.domain.learnermain.InquiryVO;
import com.goott5.lms.test.domain.learnermain.NoticeVO;
import com.goott5.lms.test.domain.learnermain.QnAVO;
import com.goott5.lms.test.domain.learnermain.TestHwScheduleVO;
import com.goott5.lms.test.domain.learnermain.TestVO;
import com.goott5.lms.test.mapper.learnermain.LearnerMainMapper;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerMainServiceImpl implements LearnerMainService {

  private final LearnerMainMapper learnerMainMapper;
  private final ParticipationMapper participationMapper;

  @Override
  public Map<String, Object> getAttendanceRateAndCourseProgress(String courseName,
      HttpSession session) {

    Integer learnerEnrollmentId = learnerMainMapper.selectLearnerEnrollmentId(courseName,
        ((UserVO) session.getAttribute("loginUser")).getId());

    // 출석일수 조회
    Integer attendanceDays = participationMapper.selectAttendanceDaysByLearnerEnrollmentId(
        learnerEnrollmentId);
    if (attendanceDays == null) {
      attendanceDays = 0;
    }

    // 전체 수업일수 조회
    Integer totalClassDays = participationMapper.selectTotalClassDaysByLearnerEnrollmentId(
        learnerEnrollmentId);
    if (totalClassDays == null || totalClassDays == 0) {
      return Map.of(
          "attendanceDays", attendanceDays,
          "totalClassDays", 0,
          "attendanceRate", 0.0,
          "totalDays", 0,
          "progressedDays", 0,
          "progressRate", 0.0,
          "baseDate", LocalDate.now().minusDays(1)
      );
    }

    // 출석률 계산
    double attendanceRate = (double) attendanceDays / totalClassDays * 100;
    attendanceRate = Math.round(attendanceRate * 10.0) / 10.0;

    // 과정 진행률 계산
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String yesterdayStr = yesterday.toString();

    Integer totalDays = participationMapper.selectTotalCourseDaysByLearnerEnrollmentId(
        learnerEnrollmentId);
    if (totalDays == null) {
      totalDays = 0;
    }

    Integer progressedDays = participationMapper.selectProgressedDaysUntilDate(learnerEnrollmentId,
        yesterdayStr);
    if (progressedDays == null) {
      progressedDays = 0;
    }

    double progressRate = totalDays > 0 ? (double) progressedDays / totalDays * 100 : 0.0;
    progressRate = Math.round(progressRate * 10.0) / 10.0;

    // 결과 반환
    return Map.of(
        "attendanceDays", attendanceDays,
        "totalClassDays", totalClassDays,
        "attendanceRate", attendanceRate,
        "totalDays", totalDays,
        "progressedDays", progressedDays,
        "progressRate", progressRate,
        "baseDate", yesterday
    );
  }

  @Override
  public List<CourseScheduleVO> getCourseSchedule(String courseName) {

    Integer courseId = learnerMainMapper.selectCourseId(courseName);

    return learnerMainMapper.selectCourseSchedule(LocalDate.now(), courseId);
  }

  @Override
  public Map<String, Object> getTestHomeworkData(String courseName, HttpSession session) {

    Integer courseId = learnerMainMapper.selectCourseId(courseName);
    int userId = ((UserVO) session.getAttribute("loginUser")).getId();

    Integer testAvgScore = learnerMainMapper.selectMyTestAvgScore(courseId, userId);
    Map<String, Object> hwData = learnerMainMapper.selectMyHwData(courseId, userId);

    Map<String, Object> result = new HashMap<>();
    result.put("testAvgScore", testAvgScore != null ? testAvgScore : 0);
    result.putAll(hwData);

    return result;
  }


  @Override
  public List<TestHwScheduleVO> getTestHwSchedule(String courseName, HttpSession session) {

    Integer courseId = learnerMainMapper.selectCourseId(courseName);
    int userId = ((UserVO) session.getAttribute("loginUser")).getId();

    return learnerMainMapper.selectHwTestSchedule(courseId, userId);
  }

  @Override
  public List<AttendanceStatusVO> getAttendanceStatus(String courseName, HttpSession session) {

    Integer learnerEnrollmentId = learnerMainMapper.selectLearnerEnrollmentId(courseName,
        ((UserVO) session.getAttribute("loginUser")).getId());

    log.info("courseName: {}", courseName);
    log.info("learnerEnrollmentId:{}", learnerEnrollmentId);
    log.info("localdate:{}", LocalDate.now());

    return learnerMainMapper.selectAttendanceStatus(learnerEnrollmentId, LocalDate.now());
  }

  @Override
  public List<InquiryVO> getInquiry(HttpSession session) {
    return learnerMainMapper.selectInquiryBoard(
        ((UserVO) session.getAttribute("loginUser")).getId());
  }

  @Override
  public List<QnAVO> getQnA(HttpSession session) {
    return learnerMainMapper.selectQnABoard(((UserVO) session.getAttribute("loginUser")).getId());
  }

  @Override
  public List<NoticeVO> getNotice(String courseName) {
    return learnerMainMapper.selectNoticeBoard(learnerMainMapper.selectCourseId(courseName));
  }

  @Override
  public List<ForumVO> getForum(String courseName) {
    return learnerMainMapper.selectForumBoard(learnerMainMapper.selectCourseId(courseName));
  }

  @Override
  public List<TestVO> getTestStatistic(String courseName, HttpSession session) {
    return learnerMainMapper.selectTestStatistic(learnerMainMapper.selectCourseId(courseName),
        ((UserVO) session.getAttribute("loginUser")).getId());
  }


}
