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

/**
 * 출결(Participation) 기본 CRUD 서비스 구현체
 * 주로 AttendanceService에서 비즈니스 로직 처리하고, 이 클래스는 기본 CRUD만 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationServiceImpl implements ParticipationService {

  private final ParticipationMapper participationMapper;

  /**
   * 출결 기록 생성 (기본 INSERT)
   * @param dto 출결 정보 DTO
   * @return 생성된 레코드 수
   */
  @Override
  public int createParticipation(ParticipationDTO dto) {
    log.info("출결 기록 생성: learnerEnrollmentId={}, date={}",
        dto.getLearnerEnrollmentId(), dto.getParticipationDate());
    return participationMapper.insertParticipation(dto);
  }

  /**
   * 출결 기록 단건 조회 (ID 기준)
   * @param id 출결 기록 ID
   * @return 출결 정보 VO
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationVO getParticipationById(Integer id) {
    log.debug("출결 기록 조회: id={}", id);
    return participationMapper.selectParticipationById(id);
  }

  /**
   * 날짜별 출결 기록 전체 조회
   * @param participationDate 조회할 날짜
   * @return 해당 날짜의 모든 출결 기록 리스트
   */
  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getParticipationByDate(LocalDate participationDate) {
    log.debug("날짜별 출결 기록 조회: date={}", participationDate);
    return participationMapper.selectParticipationByDate(participationDate);
  }

  /**
   * 전체 출결 기록 조회
   * @return 모든 출결 기록 리스트
   */
  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getAllParticipation() {
    log.debug("전체 출결 기록 조회");
    return participationMapper.selectAllParticipation();
  }

  /**
   * 전체 출결 기록 개수 조회
   * @return 총 출결 기록 개수
   */
  @Override
  @Transactional(readOnly = true)
  public int getTotalCount() {
    return participationMapper.countAll();
  }

  /**
   * 특정 과정의 모든 수강생에게 출결 기록 일괄 생성
   * 스케줄러에서 호출되며, 이미 출결 기록이 있는 학생은 건드리지 않음
   * @param courseId 과정 ID
   * @param participationDate 출결 날짜
   * @return 생성된 출결 기록 개수
   */
  @Override
  public int createDailyParticipationForCourse(Integer courseId, LocalDate participationDate) {
    log.info("과정 {}의 모든 수강생에게 출결 기록 생성 시작: {}", courseId, participationDate);

    // 해당 과정의 수강 중인 모든 learner_enrollment_id 조회
    List<Integer> learnerEnrollmentIds = participationMapper.selectActiveLearnerEnrollmentIdsByCourse(courseId);
    log.info("수강 중인 학생 수: {}", learnerEnrollmentIds.size());

    int createdCount = 0;
    for (Integer learnerEnrollmentId : learnerEnrollmentIds) {
      // 이미 오늘 출결 기록이 있는지 확인 (휴가 승인받은 학생 등 보호)
      boolean exists = participationMapper.existsParticipationToday(learnerEnrollmentId, participationDate);

      if (!exists) {
        // 기본 출결 기록 생성 (결석 상태로 시작)
        ParticipationDTO dto = ParticipationDTO.builder()
            .learnerEnrollmentId(learnerEnrollmentId)
            .status("ABSENCE") // 기본값: 결석 (입실하면 변경됨)
            .trainingTime(0) // 기본값: 0시간
            .participationDate(participationDate)
            .build();
        participationMapper.insertParticipation(dto);
        createdCount++;
        log.debug("출결 기록 생성: learnerEnrollmentId={}, participationId={}",
            learnerEnrollmentId, dto.getId());
      } else {
        log.debug("이미 출결 기록 존재 (휴가 등): learnerEnrollmentId={}", learnerEnrollmentId);
      }
    }

    log.info("과정 {}의 출결 기록 생성 완료: {}건", courseId, createdCount);
    return createdCount;
  }

  /**
   * 특정 교육생의 특정 날짜 출결 기록 조회
   * @param learnerEnrollmentId 교육생 수강 ID
   * @param participationDate 조회할 날짜
   * @return 해당 교육생의 출결 기록
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationVO getTodayParticipation(Integer learnerEnrollmentId, LocalDate participationDate) {
    log.debug("교육생 출결 기록 조회: learnerEnrollmentId={}, date={}", learnerEnrollmentId, participationDate);
    return participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
  }

  /**
   * 입실 처리 (간단 버전)
   * 실제로는 AttendanceService의 processCheckIn 사용 권장
   * @param learnerEnrollmentId 교육생 수강 ID
   * @param checkInTime 입실 시간
   * @param participationDate 출결 날짜
   * @return 처리 성공 여부
   */
  @Override
  public boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime, LocalDate participationDate) {
    log.info("입실 처리 시작: learnerEnrollmentId={}, checkInTime={}", learnerEnrollmentId, checkInTime);

    // 오늘의 출결 기록 조회
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
    if (participation == null) {
      log.warn("출결 기록이 존재하지 않음: learnerEnrollmentId={}, date={}", learnerEnrollmentId, participationDate);
      return false;
    }

    if (participation.getCheckIn() != null) {
      log.warn("이미 입실 처리됨: learnerEnrollmentId={}, 기존 입실시간={}", learnerEnrollmentId, participation.getCheckIn());
      return false;
    }

    // 입실 시간 업데이트 (상태는 퇴실 시 최종 결정)
    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status("ABSENCE") // 퇴실 전까지는 결석 상태 유지
        .checkIn(checkInTime)
        .checkOut(participation.getCheckOut())
        .trainingTime(participation.getTrainingTime())
        .participationDate(participation.getParticipationDate())
        .build();

    int result = participationMapper.updateParticipation(updateDto);
    log.info("입실 처리 완료: learnerEnrollmentId={}, 결과={}", learnerEnrollmentId, result > 0 ? "성공" : "실패");
    return result > 0;
  }

  /**
   * 퇴실 처리 (간단 버전)
   * 실제로는 AttendanceService의 processCheckOut 사용 권장 (복잡한 상태 판정 로직 포함)
   * @param learnerEnrollmentId 교육생 수강 ID
   * @param checkOutTime 퇴실 시간
   * @param participationDate 출결 날짜
   * @return 처리 성공 여부
   */
  @Override
  public boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime, LocalDate participationDate) {
    log.info("퇴실 처리 시작: learnerEnrollmentId={}, checkOutTime={}", learnerEnrollmentId, checkOutTime);

    // 오늘의 출결 기록 조회
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
    if (participation == null) {
      log.warn("출결 기록이 존재하지 않음: learnerEnrollmentId={}, date={}", learnerEnrollmentId, participationDate);
      return false;
    }

    if (participation.getCheckIn() == null) {
      log.warn("입실 기록이 없어 퇴실 불가: learnerEnrollmentId={}", learnerEnrollmentId);
      return false;
    }

    if (participation.getCheckOut() != null) {
      log.warn("이미 퇴실 처리됨: learnerEnrollmentId={}, 기존 퇴실시간={}", learnerEnrollmentId, participation.getCheckOut());
      return false;
    }

    // 퇴실 시간 업데이트 및 최종 상태 결정 (간단 버전)
    // 실제로는 AttendanceService에서 복잡한 시간 계산 로직 사용
    String finalStatus = "ATTENDANCE"; // 간단하게 출석으로 처리
    Integer finalTrainingTime = 8; // 간단하게 8시간으로 처리

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

  /**
   * 특정 과정의 수강 중인 교육생 ID 목록 조회
   * @param courseId 과정 ID
   * @return 수강 중인 교육생 수강 ID 리스트
   */
  @Override
  @Transactional(readOnly = true)
  public List<Integer> getActiveLearnerEnrollmentIds(Integer courseId) {
    log.debug("과정 {}의 수강 중인 교육생 ID 목록 조회", courseId);
    return participationMapper.selectActiveLearnerEnrollmentIdsByCourse(courseId);
  }
}
