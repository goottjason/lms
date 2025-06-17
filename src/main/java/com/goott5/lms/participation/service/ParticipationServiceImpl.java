package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationServiceImpl implements ParticipationService {

  private final ParticipationMapper participationMapper;

  @Override
  public int createParticipation(ParticipationDTO dto) {
    return participationMapper.insertParticipation(dto);
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipationVO getParticipationById(Integer id) {
    return participationMapper.selectParticipationById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getParticipationByDate(LocalDate participationDate) {
    return participationMapper.selectParticipationByDate(participationDate);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getAllParticipation() {
    return participationMapper.selectAllParticipation();
  }

  @Override
  @Transactional(readOnly = true)
  public int getTotalCount() {
    return participationMapper.countAll();
  }

  // 추가 비즈니스 메서드들

  @Override
  public int createDailyParticipationForCourse(Integer courseId, LocalDate participationDate) {
    log.info("과정 {}의 모든 수강생에게 출결 기록 생성 시작: {}", courseId, participationDate);

    // 해당 과정의 수강 중인 모든 learner_enrollment_id 조회
    List<Integer> learnerEnrollmentIds = participationMapper.selectActiveLearnerEnrollmentIdsByCourse(
        courseId);

    log.info("수강 중인 학생 수: {}", learnerEnrollmentIds.size());

    int createdCount = 0;

    for (Integer learnerEnrollmentId : learnerEnrollmentIds) {
      // 이미 오늘 출결 기록이 있는지 확인
      boolean exists = participationMapper.existsParticipationToday(learnerEnrollmentId,
          participationDate);

      if (!exists) {
        // 기본 출결 기록 생성
        ParticipationDTO dto = ParticipationDTO.builder()
            .learnerEnrollmentId(learnerEnrollmentId)
            .status("ABSENCE")           // 기본값: 결석
            .trainingTime(0)            // 기본값: 0시간
            .participationDate(participationDate)
            .build();

        participationMapper.insertParticipation(dto);
        createdCount++;
        log.debug("출결 기록 생성: learnerEnrollmentId={}, participationId={}", learnerEnrollmentId,
            dto.getId());
      } else {
        log.debug("이미 출결 기록 존재: learnerEnrollmentId={}", learnerEnrollmentId);
      }
    }

    log.info("과정 {}의 출결 기록 생성 완료: {}건", courseId, createdCount);
    return createdCount;
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipationVO getTodayParticipation(Integer learnerEnrollmentId,
      LocalDate participationDate) {
    return participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId,
        participationDate);
  }

  @Override
  public boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate) {
    log.info("입실 처리 시작: learnerEnrollmentId={}, checkInTime={}", learnerEnrollmentId, checkInTime);

    // 오늘의 출결 기록 조회
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);

    if (participation == null) {
      log.warn("출결 기록이 존재하지 않음: learnerEnrollmentId={}, date={}", learnerEnrollmentId,
          participationDate);
      return false;
    }

    if (participation.getCheckIn() != null) {
      log.warn("이미 입실 처리됨: learnerEnrollmentId={}, 기존 입실시간={}", learnerEnrollmentId,
          participation.getCheckIn());
      return false;
    }

    // 입실 시간 업데이트
    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status("ON_TIME")  // 임시 상태 (실제로는 수업 시작 시간과 비교해야 함)
        .checkIn(checkInTime)
        .checkOut(participation.getCheckOut())
        .trainingTime(participation.getTrainingTime())
        .participationDate(participation.getParticipationDate())
        .build();

    int result = participationMapper.updateParticipation(updateDto);

    log.info("입실 처리 완료: learnerEnrollmentId={}, 결과={}", learnerEnrollmentId,
        result > 0 ? "성공" : "실패");
    return result > 0;
  }

  @Override
  public boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime,
      LocalDate participationDate) {
    log.info("퇴실 처리 시작: learnerEnrollmentId={}, checkOutTime={}", learnerEnrollmentId,
        checkOutTime);

    // 오늘의 출결 기록 조회
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);

    if (participation == null) {
      log.warn("출결 기록이 존재하지 않음: learnerEnrollmentId={}, date={}", learnerEnrollmentId,
          participationDate);
      return false;
    }

    if (participation.getCheckIn() == null) {
      log.warn("입실 기록이 없어 퇴실 불가: learnerEnrollmentId={}", learnerEnrollmentId);
      return false;
    }

    if (participation.getCheckOut() != null) {
      log.warn("이미 퇴실 처리됨: learnerEnrollmentId={}, 기존 퇴실시간={}", learnerEnrollmentId,
          participation.getCheckOut());
      return false;
    }

    // 퇴실 시간 업데이트 및 최종 상태 결정
    String finalStatus = "ATTENDANCE";  // 간단하게 출석으로 처리 (실제로는 복잡한 로직 필요)
    Integer finalTrainingTime = 8;      // 간단하게 8시간으로 처리

    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status(finalStatus)
        .checkIn(participation.getCheckIn())
        .checkOut(checkOutTime)
        .trainingTime(finalTrainingTime)
        .participationDate(participation.getParticipationDate())
        .build();

    int result = participationMapper.updateParticipation(updateDto);

    log.info("퇴실 처리 완료: learnerEnrollmentId={}, 최종상태={}, 인정시간={}시간, 결과={}",
        learnerEnrollmentId, finalStatus, finalTrainingTime, result > 0 ? "성공" : "실패");
    return result > 0;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Integer> getActiveLearnerEnrollmentIds(Integer courseId) {
    return participationMapper.selectActiveLearnerEnrollmentIdsByCourse(courseId);
  }
}
