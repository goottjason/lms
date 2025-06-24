package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ParticipationServiceTest {

  // 기본 CRUD
  int createParticipation(ParticipationDTO dto);

  ParticipationVO getParticipationById(Integer id);

  List<ParticipationVO> getParticipationByDate(LocalDate participationDate);

  List<ParticipationVO> getAllParticipation();

  int getTotalCount();

  // 추가 비즈니스 메서드들

  // 특정 과정의 모든 수강생에게 출결 기록 생성 (스케줄러 대신)
  int createDailyParticipationForCourse(Integer courseId, LocalDate participationDate);

  // 특정 학생의 오늘 출결 기록 조회
  ParticipationVO getTodayParticipation(Integer learnerEnrollmentId, LocalDate participationDate);

  // 입실 처리
  boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate);

  // 퇴실 처리
  boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime,
      LocalDate participationDate);

  // 특정 과정의 수강 중인 learner_enrollment_id 목록 조회
  List<Integer> getActiveLearnerEnrollmentIds(Integer courseId);
}
