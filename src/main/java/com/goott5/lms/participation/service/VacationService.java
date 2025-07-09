package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationReasonVO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

/**
 * 휴가(Vacation) 서비스 인터페이스 휴가 신청/승인/거부/삭제 등 제공
 */
public interface VacationService {

  // 휴가 신청 (VACATION_PENDING 상태로 participation/사유서 생성)
  boolean applyVacation(Integer learnerEnrollmentId, LocalDate vacationDate, String explanation);

  // 휴가 승인 (VACATION_PENDING → VACATION)
  boolean approveVacation(Integer participationId);

  // 휴가 거부 (VACATION_PENDING → 삭제)
  boolean rejectVacation(Integer participationId);

  // 휴가 삭제 (승인된 휴가)
  boolean deleteVacation(Integer participationId);

  // 휴가 사유 조회
  ParticipationReasonVO getVacationReason(Integer participationId);

  /**
   * 강사가 맡은 과정 목록 조회 (현재/과거)
   */
  Map<String, Object> getInstructorCourses(Integer instructorId);

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 조회 (현재 과정용)
   */
  Page<Map<String, Object>> getPendingVacationsByCourse(Integer courseId, int page, int size);

  /**
   * 과정별 승인된 휴가 목록 조회 (과거 과정용)
   */
  Page<Map<String, Object>> getApprovedVacationsByCourse(Integer courseId, int page, int size);

  // 현재 과정용 - 승인 대기 + 승인된 휴가 모두 조회
  Page<Map<String, Object>> getAllVacationsByCourse(Integer courseId, int page, int size);

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 검색 (이름으로)
   */
  Page<Map<String, Object>> searchPendingVacationsByCourse(Integer courseId, int page, int size, String searchName);

  /**
   * 과정별 승인된 휴가 목록 검색 (이름으로)
   */
  Page<Map<String, Object>> searchApprovedVacationsByCourse(Integer courseId, int page, int size, String searchName);

  /**
   * 과정별 모든 휴가 목록 검색 (이름으로)
   */
  Page<Map<String, Object>> searchAllVacationsByCourse(Integer courseId, int page, int size, String searchName);


  int getLearnerIdByParticipationId(Integer participationId);

  LocalDate getVacationDateByParticipationId(Integer participationId);
}
