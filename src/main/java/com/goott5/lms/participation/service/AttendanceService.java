package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 출결(participation) 서비스 인터페이스
 */
public interface AttendanceService {

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

  // 진행률 계산
  double getProgressPercentage(Integer learnerEnrollmentId);

  // 새로 추가: 사용자 ID로 learnerEnrollmentId 조회
  Integer getLearnerEnrollmentIdByUserId(Integer userId);
}
