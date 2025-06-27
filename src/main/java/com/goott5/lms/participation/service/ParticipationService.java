package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 출결(participation) 서비스 인터페이스
 */
public interface ParticipationService {

  // 모든 과정의 출결 기록 생성 (스케줄러)
  void createDailyAttendanceForAllCourses(LocalDate participationDate);

  // 특정 과정의 출결 기록 생성
  int createDailyAttendanceForCourse(Integer courseId, LocalDate participationDate);

  // 입실 처리
  boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime, LocalDate participationDate);

  // 퇴실 처리
  boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime, LocalDate participationDate);

  // 오늘 출결 기록 + 화면 표시 상태
  ParticipationVO getTodayParticipationWithDisplayStatus(Integer learnerEnrollmentId, LocalDate participationDate);

  // 날짜별 출결 기록 + 화면 표시 상태
  List<ParticipationVO> getParticipationByDateWithDisplayStatus(LocalDate participationDate);

  // 퇴실 예상 상태 예측
  String predictAttendanceStatus(Integer learnerEnrollmentId, LocalDateTime predictedCheckOut, LocalDate participationDate);

  // course_schedule 기반 수업일 여부 확인
  boolean isClassDay(LocalDate date);

  // 새로 추가: 사용자 ID로 learnerEnrollmentId 조회
  Integer getLearnerEnrollmentIdByUserId(Integer userId);

  /**
   * 교육생의 출석률 계산 (출석일수/전체수업일수 * 100)
   * 기존 getProgressPercentage에서 변경
   */
  double getAttendanceRate(Integer learnerEnrollmentId);

  CourseVO getCurrentCourseByUserId(Integer userId);

  List<CourseVO> getPreviousCoursesByUserId(Integer userId);

  List<ParticipationVO> getParticipationByLearnerEnrollmentIdAndDateRange(
      Integer learnerEnrollmentId, LocalDate startDate, LocalDate endDate);

  /**
   * 이전 날짜의 미완료 출결 기록들을 결석 처리
   */
  void processIncompleteRecordsFromPreviousDays(LocalDate currentDate);

  /**
   * 어제까지의 출결 통계 조회
   */
  Map<String, Object> getAttendanceStatsUntilYesterday(Integer learnerEnrollmentId);

  /**
   * 어제까지의 과정 진행률 조회 (실제 진행일수 기준)
   */
  Map<String, Object> getCourseProgressUntilYesterday(Integer learnerEnrollmentId);

  /**
   * 사용자 ID와 과정 ID로 learnerEnrollmentId 조회
   */
  Integer getLearnerEnrollmentIdByUserIdAndCourseId(Integer userId, Integer courseId);

  /**
   * 휴가 신청 가능한 날짜 목록 조회
   * (오늘 이후 + course_schedule에 있는 날짜 + 과정 종료일 이전)
   */
  List<LocalDate> getAvailableVacationDates(Integer learnerEnrollmentId);


}
