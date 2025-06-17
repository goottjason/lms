package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationReasonVO;
import java.time.LocalDate;

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
}
