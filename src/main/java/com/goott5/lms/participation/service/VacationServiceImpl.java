package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationReasonDTO;
import com.goott5.lms.participation.domain.ParticipationReasonVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationMapper;
import com.goott5.lms.participation.mapper.ParticipationReasonMapper;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 휴가(Vacation) 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VacationServiceImpl implements VacationService {

  private final ParticipationMapper participationMapper;
  private final ParticipationReasonMapper participationReasonMapper;

  /**
   * 휴가 신청 (VACATION_PENDING 상태로 participation/사유서 생성) 변경 없음
   */
  @Override
  public boolean applyVacation(Integer learnerEnrollmentId, LocalDate vacationDate,
      String explanation) {
    log.info("휴가 신청 시작: learnerEnrollmentId={}, vacationDate={}", learnerEnrollmentId,
        vacationDate);

    try {
      ParticipationVO existing = participationMapper.selectByLearnerEnrollmentIdAndDate(
          learnerEnrollmentId, vacationDate);

      if (existing != null) {
        log.warn("이미 해당 날짜에 출결 기록이 존재함: {}", vacationDate);
        return false;
      }

      ParticipationDTO participationDTO = ParticipationDTO.builder()
          .learnerEnrollmentId(learnerEnrollmentId)
          .status("VACATION_PENDING")
          .trainingTime(0)
          .participationDate(vacationDate)
          .build();

      participationMapper.insertParticipation(participationDTO);

      ParticipationReasonDTO reasonDTO = ParticipationReasonDTO.builder()
          .participationId(participationDTO.getId())
          .explanation(explanation)
          .build();

      participationReasonMapper.insertParticipationReason(reasonDTO);

      log.info("휴가 신청 완료: participationId={}, reasonId={}", participationDTO.getId(),
          reasonDTO.getId());
      return true;

    } catch (Exception e) {
      log.error("휴가 신청 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 승인 (VACATION_PENDING → VACATION) 변경 없음
   */
  @Override
  public boolean approveVacation(Integer participationId) {
    log.info("휴가 승인 처리 시작: participationId={}", participationId);

    try {
      ParticipationVO participation = participationMapper.selectParticipationById(participationId);

      if (participation == null || !"VACATION_PENDING".equals(participation.getStatus())) {
        log.warn("승인 처리할 수 없는 출결 기록: participationId={}, status={}", participationId,
            participation != null ? participation.getStatus() : "null");
        return false;
      }

      ParticipationDTO updateDTO = ParticipationDTO.builder()
          .id(participation.getId())
          .learnerEnrollmentId(participation.getLearnerEnrollmentId())
          .status("VACATION")
          .trainingTime(8)
          .participationDate(participation.getParticipationDate())
          .build();

      participationMapper.updateParticipation(updateDTO);

      log.info("휴가 승인 처리 완료: participationId={}", participationId);
      return true;

    } catch (Exception e) {
      log.error("휴가 승인 처리 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 거부 (VACATION_PENDING → 하드 딜리트) 수정: deleteParticipation/deleteParticipationReason이 하드 딜리트로
   * 동작
   */
  @Override
  public boolean rejectVacation(Integer participationId) {
    log.info("휴가 거부 처리 시작 (하드 딜리트): participationId={}", participationId);

    try {
      ParticipationVO participation = participationMapper.selectParticipationById(participationId);

      if (participation == null || !"VACATION_PENDING".equals(participation.getStatus())) {
        log.warn("거부 처리할 수 없는 출결 기록: participationId={}, status={}", participationId,
            participation != null ? participation.getStatus() : "null");
        return false;
      }

      // 1. 사유서 하드 딜리트
      ParticipationReasonVO reason = participationReasonMapper.selectReasonByParticipationId(
          participationId);
      if (reason != null) {
        participationReasonMapper.deleteParticipationReason(reason.getId());
        log.debug("사유서 하드 딜리트 완료: reasonId={}", reason.getId());
      }

      // 2. 출결 기록 하드 딜리트
      participationMapper.deleteParticipation(participationId);

      log.info("휴가 거부 처리 완료 (하드 딜리트): participationId={}", participationId);
      return true;

    } catch (Exception e) {
      log.error("휴가 거부 처리 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 삭제 (교육생이 직접 삭제 - 하드 딜리트) 수정: deleteParticipation/deleteParticipationReason이 하드 딜리트로 동작
   */
  @Override
  public boolean deleteVacation(Integer participationId) {
    log.info("휴가 삭제 시작 (하드 딜리트): participationId={}", participationId);

    try {
      ParticipationVO participation = participationMapper.selectParticipationById(participationId);

      if (participation == null) {
        log.warn("삭제할 출결 기록이 존재하지 않음: participationId={}", participationId);
        return false;
      }

      if (!"VACATION".equals(participation.getStatus()) && !"VACATION_PENDING".equals(
          participation.getStatus())) {
        log.warn("삭제할 수 없는 출결 기록 상태: participationId={}, status={}", participationId,
            participation.getStatus());
        return false;
      }

      // 1. 사유서 하드 딜리트
      ParticipationReasonVO reason = participationReasonMapper.selectReasonByParticipationId(
          participationId);
      if (reason != null) {
        participationReasonMapper.deleteParticipationReason(reason.getId());
        log.debug("사유서 하드 딜리트 완료: reasonId={}", reason.getId());
      }

      // 2. 출결 기록 하드 딜리트
      participationMapper.deleteParticipation(participationId);

      log.info("휴가 삭제 완료 (하드 딜리트): participationId={}", participationId);
      return true;

    } catch (Exception e) {
      log.error("휴가 삭제 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 사유 조회 변경 없음
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationReasonVO getVacationReason(Integer participationId) {
    return participationReasonMapper.selectReasonByParticipationId(participationId);
  }
}
