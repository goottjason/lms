package com.goott5.lms.test.mapper.learnermain;

import com.goott5.lms.test.domain.learnermain.AttendanceStatusVO;
import com.goott5.lms.test.domain.learnermain.CourseScheduleVO;
import com.goott5.lms.test.domain.learnermain.ForumVO;
import com.goott5.lms.test.domain.learnermain.InquiryVO;
import com.goott5.lms.test.domain.learnermain.NoticeVO;
import com.goott5.lms.test.domain.learnermain.QnAVO;
import com.goott5.lms.test.domain.learnermain.TestHwScheduleVO;
import com.goott5.lms.test.domain.learnermain.TestVO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearnerMainMapper {

  Integer selectCourseId(String courseName);

  Integer selectLearnerEnrollmentId(@Param("courseName") String courseName,
      @Param("userId") int userId);

  // 시험 평균 점수
  Integer selectMyTestAvgScore(@Param("courseId") int courseId, @Param("userId") int userId);

  // 과제 data
  Map<String, Object> selectMyHwData(@Param("courseId") int courseId, @Param("userId") int userId);

  // 과정 일정 data
  List<CourseScheduleVO> selectCourseSchedule(@Param("today") LocalDate today,
      @Param("courseId") int courseId);

  // 시험, 과제 일정 data
  List<TestHwScheduleVO> selectHwTestSchedule(@Param("courseId") int courseId,
      @Param("userId") int userId);

  // 출석 상태 data
  List<AttendanceStatusVO> selectAttendanceStatus(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId, @Param("today") LocalDate today);

  // 1대1 문의 data
  List<InquiryVO> selectInquiryBoard(int userId);

  // qna data
  List<QnAVO> selectQnABoard(int userId);

  // 과정 공지 data
  List<NoticeVO> selectNoticeBoard(int courseId);

  // 토론 인기글 data
  List<ForumVO> selectForumBoard(int courseId);

  // 시험 통계 data
  List<TestVO> selectTestStatistic(@Param("courseId") int courseId, @Param("userId") int userId);
}
