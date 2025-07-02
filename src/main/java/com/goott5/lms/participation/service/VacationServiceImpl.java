package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationReasonDTO;
import com.goott5.lms.participation.domain.ParticipationReasonVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.mapper.ParticipationMapper;
import com.goott5.lms.participation.mapper.ParticipationReasonMapper;
import com.goott5.lms.participation.util.TimeCalculationUtil;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private final ParticipationCourseMapper participationCourseMapper;

  /**
   * 휴가 신청 (VACATION_PENDING 상태로 participation/사유서 생성)
   */
  @Override
  public boolean applyVacation(Integer learnerEnrollmentId, LocalDate vacationDate, String explanation) {
    log.info("휴가 신청 시작: learnerEnrollmentId={}, vacationDate={}", learnerEnrollmentId, vacationDate);

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

      log.info("휴가 신청 완료: participationId={}, reasonId={}", participationDTO.getId(), reasonDTO.getId());
      return true;
    } catch (Exception e) {
      log.error("휴가 신청 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 승인 (VACATION_PENDING → VACATION, 과정별 daily_hours 반영)
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

      // 해당 교육생의 과정 정보 조회
      CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(participation.getLearnerEnrollmentId());

      // 과정별 daily_hours를 사용하여 휴가 인정시간 계산
      int vacationTrainingTime = TimeCalculationUtil.calculateTrainingTime("VACATION",
          course != null ? course.getDailyHours() : null);

      ParticipationDTO updateDTO = ParticipationDTO.builder()
          .id(participation.getId())
          .learnerEnrollmentId(participation.getLearnerEnrollmentId())
          .status("VACATION")
          .trainingTime(vacationTrainingTime)
          .participationDate(participation.getParticipationDate())
          .build();

      participationMapper.updateParticipation(updateDTO);

      log.info("휴가 승인 처리 완료: participationId={}, 인정시간={}시간", participationId, vacationTrainingTime);
      return true;
    } catch (Exception e) {
      log.error("휴가 승인 처리 실패: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 휴가 거부 (VACATION_PENDING → 하드 딜리트)
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
      ParticipationReasonVO reason = participationReasonMapper.selectReasonByParticipationId(participationId);
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
   * 휴가 삭제 (교육생이 직접 삭제 - 하드 딜리트)
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

      if (!"VACATION".equals(participation.getStatus()) && !"VACATION_PENDING".equals(participation.getStatus())) {
        log.warn("삭제할 수 없는 출결 기록 상태: participationId={}, status={}", participationId,
            participation.getStatus());
        return false;
      }

      // 1. 사유서 하드 딜리트
      ParticipationReasonVO reason = participationReasonMapper.selectReasonByParticipationId(participationId);
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
   * 휴가 사유 조회
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationReasonVO getVacationReason(Integer participationId) {
    return participationReasonMapper.selectReasonByParticipationId(participationId);
  }


  /**
   * 강사가 맡은 과정 목록 조회 (현재/과거) - staff_assignment 기반
   */
  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getInstructorCourses(Integer instructorId) {
    try {
      Map<String, Object> result = new HashMap<>();
      Map<String, Object> currentCourse = participationMapper.selectCurrentCourseByInstructor(instructorId);
      List<Map<String, Object>> previousCourses = participationMapper.selectPreviousCoursesByInstructor(instructorId);

      if (currentCourse != null) {
        int pendingCount = participationMapper.countPendingVacationsByCourse(
            (Integer) currentCourse.get("id")
        );
        currentCourse.put("pendingCount", pendingCount);
      }

      result.put("currentCourse", currentCourse);
      result.put("previousCourses", previousCourses != null ? previousCourses : List.of());

      log.debug("강사 과정 목록 조회 완료 - instructorId: {}, 현재과정: {}, 과거과정: {}건",
          instructorId, currentCourse != null ? currentCourse.get("name") : "없음", previousCourses.size());

      return result;
    } catch (Exception e) {
      log.error("강사 과정 목록 조회 중 오류 - instructorId: {}", instructorId, e);
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("currentCourse", null);
      errorResult.put("previousCourses", List.of());
      return errorResult;
    }
  }

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 조회 (현재 과정용)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> getPendingVacationsByCourse(Integer courseId, int page, int size) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));

      int totalCount = participationMapper.countPendingVacationsByCourse(courseId);
      List<Map<String, Object>> pendingVacations = participationMapper.selectPendingVacationsByCourseWithPaging(
          courseId, pageable.getOffset(), pageable.getPageSize());

      log.debug("과정별 승인 대기 휴가 조회 완료 - courseId: {}, 페이지: {}, 크기: {}, 전체: {}건",
          courseId, page, size, totalCount);

      return new PageImpl<>(pendingVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 승인 대기 휴가 목록 조회 중 오류 - courseId: {}", courseId, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }

  /**
   * 과정별 승인된 휴가 목록 조회 (과거 과정용)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> getApprovedVacationsByCourse(Integer courseId, int page, int size) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));

      int totalCount = participationMapper.countApprovedVacationsByCourse(courseId);
      List<Map<String, Object>> approvedVacations = participationMapper.selectApprovedVacationsByCourseWithPaging(
          courseId, pageable.getOffset(), pageable.getPageSize());

      log.debug("과정별 승인된 휴가 조회 완료 - courseId: {}, 페이지: {}, 크기: {}, 전체: {}건",
          courseId, page, size, totalCount);

      return new PageImpl<>(approvedVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 승인된 휴가 목록 조회 중 오류 - courseId: {}", courseId, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }

  /**
   * 과정별 모든 휴가 목록 조회 (승인 대기 + 승인된 휴가)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> getAllVacationsByCourse(Integer courseId, int page, int size) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));

      int totalCount = participationMapper.countAllVacationsByCourse(courseId);
      List<Map<String, Object>> allVacations = participationMapper.selectAllVacationsByCourseWithPaging(
          courseId, pageable.getOffset(), pageable.getPageSize());

      log.debug("과정별 모든 휴가 조회 완료 - courseId: {}, 페이지: {}, 크기: {}, 전체: {}건",
          courseId, page, size, totalCount);

      return new PageImpl<>(allVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 모든 휴가 목록 조회 중 오류 - courseId: {}", courseId, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 검색 (이름으로)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> searchPendingVacationsByCourse(Integer courseId, int page, int size, String searchName) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));
      int totalCount = participationMapper.countPendingVacationsBySearch(courseId, searchName);
      List<Map<String, Object>> pendingVacations = participationMapper.selectPendingVacationsBySearch(
          courseId, pageable.getOffset(), pageable.getPageSize(), searchName);

      log.debug("과정별 승인 대기 휴가 검색 완료 - courseId: {}, 검색어: {}, 전체: {}건",
          courseId, searchName, totalCount);
      return new PageImpl<>(pendingVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 승인 대기 휴가 검색 중 오류 - courseId: {}, 검색어: {}", courseId, searchName, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }

  /**
   * 과정별 승인된 휴가 목록 검색 (이름으로)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> searchApprovedVacationsByCourse(Integer courseId, int page, int size, String searchName) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));
      int totalCount = participationMapper.countApprovedVacationsBySearch(courseId, searchName);
      List<Map<String, Object>> approvedVacations = participationMapper.selectApprovedVacationsBySearch(
          courseId, pageable.getOffset(), pageable.getPageSize(), searchName);

      log.debug("과정별 승인된 휴가 검색 완료 - courseId: {}, 검색어: {}, 전체: {}건",
          courseId, searchName, totalCount);
      return new PageImpl<>(approvedVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 승인된 휴가 검색 중 오류 - courseId: {}, 검색어: {}", courseId, searchName, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }

  /**
   * 과정별 모든 휴가 목록 검색 (이름으로)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<Map<String, Object>> searchAllVacationsByCourse(Integer courseId, int page, int size, String searchName) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));
      int totalCount = participationMapper.countAllVacationsBySearch(courseId, searchName);
      List<Map<String, Object>> allVacations = participationMapper.selectAllVacationsBySearch(
          courseId, pageable.getOffset(), pageable.getPageSize(), searchName);

      log.debug("과정별 모든 휴가 검색 완료 - courseId: {}, 검색어: {}, 전체: {}건",
          courseId, searchName, totalCount);
      return new PageImpl<>(allVacations, pageable, totalCount);
    } catch (Exception e) {
      log.error("과정별 모든 휴가 검색 중 오류 - courseId: {}, 검색어: {}", courseId, searchName, e);
      return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
    }
  }


}
