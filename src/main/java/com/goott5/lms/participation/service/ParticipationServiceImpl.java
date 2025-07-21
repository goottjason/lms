package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.mapper.ParticipationMapper;
import com.goott5.lms.participation.util.TimeCalculationUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출결(participation) 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ParticipationServiceImpl implements ParticipationService {

  private final ParticipationMapper participationMapper;
  private final ParticipationCourseMapper participationCourseMapper;

  /**
   * 모든 과정의 출결 기록 생성 (course_schedule 기반)
   */
  @Override
  public void createDailyAttendanceForAllCourses(LocalDate participationDate) {

    // course_schedule에서 해당 날짜에 수업이 있는 과정들 조회
    // 이 결과가 비어있으면 아래 for문은 실행되지 않으므로, isClassDay 체크와 동일한 역할을함
    List<Integer> courseIds = participationCourseMapper.selectCoursesBySchedule(participationDate);
    log.info("오늘 수업이 있는 과정 수: {}", courseIds.size());

    for (Integer courseId : courseIds) {
      createDailyAttendanceForCourse(courseId, participationDate);
    }
  }

  /**
   * 특정 과정의 출결 기록 생성
   */
  @Override
  public int createDailyAttendanceForCourse(Integer courseId, LocalDate participationDate) {
    List<Integer> learnerEnrollmentIds = participationMapper.selectActiveLearnerEnrollmentIdsByCourse(courseId);
    int createdCount = 0;

    for (Integer learnerEnrollmentId : learnerEnrollmentIds) {
      if (!participationMapper.existsParticipationToday(learnerEnrollmentId, participationDate)) {
        // 출결 기록이 없을 때만 insert
        ParticipationDTO dto = ParticipationDTO.builder()
            .learnerEnrollmentId(learnerEnrollmentId)
            .status("ABSENCE")
            .trainingTime(0)
            .participationDate(participationDate)
            .build();
        participationMapper.insertParticipation(dto);
        createdCount++;
      }
      // 이미 출결 기록이 있으면 (휴가 승인받은 학생 등) 건드리지 않음
    }

    log.info("과정 {} 출결 기록 생성 완료: {}건", courseId, createdCount);
    return createdCount;
  }

  /**
   * course_schedule 기반 수업일 여부 확인
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isClassDay(Integer courseId, LocalDate date) {
    // courseId가 없으면 수업일이 아님
    if (courseId == null) {
      return false;
    }
    // 새로 만든 매퍼 메소드를 호출하여 해당 과정의 수업일이 맞는지 확인
    return participationCourseMapper.countClassDayForCourse(courseId, date) > 0;
  }

  /**
   * 사용자 ID로 learnerEnrollmentId 조회
   */
  @Override
  @Transactional(readOnly = true)
  public Integer getLearnerEnrollmentIdByUserId(Integer userId) {
    return participationCourseMapper.selectLearnerEnrollmentIdByUserId(userId);
  }

  /**
   * 입실 처리 (과정별 지각 기준 적용)
   */
  @Override
  public boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() != null) {
      return false;
    }

    // 과정 정보 조회하여 지각 여부 판단
    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
    if (course == null) {
      return false;
    }

    // 지각 여부 판단
    LocalTime checkInTimeOnly = checkInTime.toLocalTime();
    LocalTime lessonStartTime = course.getLessonStartTime();
    boolean isLate = checkInTimeOnly.isAfter(lessonStartTime);

    // 입실 시 상태 결정 (지각이면 LATE, 아니면 IN_STUDY)
    String initialStatus = isLate ? "LATE" : "IN_STUDY";

    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status(initialStatus) // 유동적 상태 설정
        .checkIn(checkInTime)
        .checkOut(participation.getCheckOut())
        .trainingTime(participation.getTrainingTime())
        .participationDate(participation.getParticipationDate())
        .build();

    return participationMapper.updateParticipation(updateDto) > 0;
  }


  /**
   * 퇴실 처리 (최종 상태/인정시간 계산)
   */
  @Override
  public boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime, LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() == null || participation.getCheckOut() != null) {
      return false;
    }

    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
    AttendanceResult result = calculateFinalAttendanceStatus(participation.getCheckIn(), checkOutTime, course);

    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status(result.getStatus())
        .checkIn(participation.getCheckIn())
        .checkOut(checkOutTime)
        .trainingTime(result.getTrainingTime())
        .participationDate(participation.getParticipationDate())
        .build();
    return participationMapper.updateParticipation(updateDto) > 0;
  }

  /**
   * 오늘 출결 기록 + 화면 표시 상태 반환
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationVO getTodayParticipationWithDisplayStatus(Integer learnerEnrollmentId, LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
    if (participation != null) {
      applyDisplayStatus(participation);
    }
    return participation;
  }

  /**
   * 날짜별 출결 기록 + 화면 표시 상태 반환
   */
  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getParticipationByDateWithDisplayStatus(LocalDate participationDate) {
    return participationMapper.selectParticipationByDate(participationDate)
        .stream().map(this::applyDisplayStatus).collect(Collectors.toList());
  }

  /**
   * 퇴실 예상 상태 예측 (과정별 daily_hours 반영)
   */
  @Override
  @Transactional(readOnly = true)
  public String predictAttendanceStatus(Integer learnerEnrollmentId, LocalDateTime predictedCheckOut, LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() == null) {
      return "ABSENCE";
    }

    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
    if (course == null) {
      return "ABSENCE";
    }

    // 과정별 시간 기준 사용
    LocalTime lessonEnd = course.getLessonEndTime();
    LocalTime lessonEndPlus10 = lessonEnd.plusMinutes(10);
    LocalTime nowTime = predictedCheckOut.toLocalTime();
    boolean isLate = "LATE".equals(participation.getStatus());

    // 과정별 daily_hours 가져오기
    Integer dailyHours = course.getDailyHours();
    int courseHours = (dailyHours != null && dailyHours > 0) ? dailyHours : 8;
    int halfCourseHours = courseHours / 2;

    // 지각자는 lesson_end_time 전까지 퇴실 불가
    if (isLate && nowTime.isBefore(lessonEnd)) {
      return "NOT_ALLOWED";
    }

    // lesson_end_time+10분 이후면 결석 처리
    if (nowTime.isAfter(lessonEndPlus10)) {
      return "ABSENCE";
    }

    // 실제 수업시간 계산
    long actualMinutes = TimeCalculationUtil.calculateActualStudyMinutes(
        participation.getCheckIn(), predictedCheckOut,
        course.getLunchStartTime(), course.getLunchEndTime());
    long actualHours = actualMinutes / 60;

    // 과정별 기준으로 상태 판정
    if (actualHours >= courseHours) {
      return "ATTENDANCE";
    } else if (actualHours >= halfCourseHours) {
      return isLate ? "LATE" : "LEAVE_EARLY";
    } else {
      return "ABSENCE";
    }
  }

  /**
   * 교육생의 출석률 계산 (%)
   * 출석일수 / 전체수업일수 * 100
   * 기존 getProgressPercentage에서 getAttendanceRate로 변경
   */
  @Override
  @Transactional(readOnly = true)
  public double getAttendanceRate(Integer learnerEnrollmentId) {
    try {
      // 교육생의 출석일수 조회 (training_time > 0인 날의 개수)
      Integer attendanceDays = participationMapper.selectAttendanceDaysByLearnerEnrollmentId(learnerEnrollmentId);
      if (attendanceDays == null) attendanceDays = 0;

      // 해당 과정의 전체 수업일수 조회
      Integer totalClassDays = participationMapper.selectTotalClassDaysByLearnerEnrollmentId(learnerEnrollmentId);
      if (totalClassDays == null || totalClassDays == 0) {
        return 0.0;
      }

      // 출석률 계산 (소수점 첫째자리까지)
      double attendanceRate = (double) attendanceDays / totalClassDays * 100;
      return Math.round(attendanceRate * 10.0) / 10.0;

    } catch (Exception e) {
      log.error("출석률 계산 중 오류 발생: learnerEnrollmentId={}", learnerEnrollmentId, e);
      return 0.0;
    }
  }

  /**
   * 최종 출결 상태 및 인정시간 계산 (과정별 daily_hours 반영)
   */
  private AttendanceResult calculateFinalAttendanceStatus(LocalDateTime checkIn, LocalDateTime checkOut, CourseVO course) {
    if (checkIn == null || checkOut == null || course == null) {
      return new AttendanceResult("ABSENCE", 0);
    }

    try {
      long actualMinutes = TimeCalculationUtil.calculateActualStudyMinutes(
              checkIn, checkOut, course.getLunchStartTime(), course.getLunchEndTime());

      long actualHours = actualMinutes / 60;
      boolean isLate = checkIn.toLocalTime().isAfter(course.getLessonStartTime());

      String status = TimeCalculationUtil.determineAttendanceStatus(actualHours, isLate, course.getDailyHours());
      int trainingTime = TimeCalculationUtil.calculateTrainingTime(status, course.getDailyHours());

      log.debug("출결 상태 계산 완료 - 실제훈련: {}시간, 지각여부: {}, 최종상태: {}, 인정시간: {}시간",
              actualHours, isLate, status, trainingTime);

      return new AttendanceResult(status, trainingTime);

    } catch (IllegalArgumentException e) {
      // daily_hours에 문제가 있을 경우, 오류 로그를 남기고 안전하게 '결석'으로 처리
      log.error("출결 상태 계산 실패: Course ID {}의 daily_hours({})가 유효하지 않습니다. 결석으로 처리합니다.",
              course.getId(), course.getDailyHours(), e);
      return new AttendanceResult("ABSENCE", 0);
    }
  }

  /**
   * 화면 표시 상태 적용
   */
  private ParticipationVO applyDisplayStatus(ParticipationVO participation) {
    String dbStatus = participation.getStatus();
    LocalDate participationDate = participation.getParticipationDate();
    LocalDate today = LocalDate.now();

    String displayStatus;
    String displayStatusText;
    boolean isStatusVisible;

    // ✅ 핵심: DB 상태를 우선적으로 확인
    if ("ABSENCE".equals(dbStatus)) {
      // ✅ 중요: 오늘 날짜이고 입실 기록이 없으면 상태 숨김
      if (participationDate.equals(today) && participation.getCheckIn() == null) {
        displayStatus = "PENDING";
        displayStatusText = "";
        isStatusVisible = false;
      } else {
        // 과거 날짜이거나 입실 기록이 있는 결석은 표시
        displayStatus = "ABSENCE";
        displayStatusText = "결석";
        isStatusVisible = true;
      }
    } else if ("VACATION".equals(dbStatus)) {
      // 휴가 상태
      displayStatus = "VACATION";
      displayStatusText = "휴가";
      isStatusVisible = true;
    } else if ("VACATION_PENDING".equals(dbStatus)) {
      // 휴가 승인대기 상태
      displayStatus = "VACATION_PENDING";
      displayStatusText = "휴가(미승인)";
      isStatusVisible = true;
    } else if ("ATTENDANCE".equals(dbStatus)) {
      // 출석 완료
      displayStatus = "ATTENDANCE";
      displayStatusText = "출석";
      isStatusVisible = true;
    } else if ("LATE".equals(dbStatus)) {
      // 지각 완료
      displayStatus = "LATE";
      displayStatusText = "지각";
      isStatusVisible = true;
    } else if ("LEAVE_EARLY".equals(dbStatus)) {
      // 조퇴 완료
      displayStatus = "LEAVE_EARLY";
      displayStatusText = "조퇴";
      isStatusVisible = true;
    } else if ("IN_STUDY".equals(dbStatus)) {
      // 수업 중 상태
      if (participation.getCheckIn() != null && participation.getCheckOut() == null) {
        displayStatus = "IN_STUDY";
        displayStatusText = "수업중";
        isStatusVisible = true;
      } else {
        // IN_STUDY인데 비정상적인 상태
        displayStatus = "PENDING";
        displayStatusText = "";
        isStatusVisible = false;
      }
    } else {
      // 기타 상태 처리
      if (participation.getCheckIn() != null && participation.getCheckOut() == null) {
        // 입실은 있지만 퇴실이 없는 경우
        if ("ABSENCE".equals(dbStatus)) {
          // ✅ 스케줄러에 의해 결석 처리된 경우 (과거 날짜)
          if (participationDate.isBefore(today)) {
            displayStatus = "ABSENCE";
            displayStatusText = "결석";
            isStatusVisible = true;
          } else {
            // 오늘 날짜라면 수업중으로 표시
            displayStatus = "IN_STUDY";
            displayStatusText = "수업중";
            isStatusVisible = true;
          }
        } else {
          // 아직 수업 중인 상태
          displayStatus = "IN_STUDY";
          displayStatusText = "수업중";
          isStatusVisible = true;
        }
      } else if (participation.getCheckIn() != null && participation.getCheckOut() != null) {
        // 입실과 퇴실이 모두 있는 경우 DB 상태 그대로 사용
        displayStatus = dbStatus;
        displayStatusText = getStatusText(dbStatus);
        isStatusVisible = true;
      } else {
        // 입실도 하지 않은 상태
        if ("ABSENCE".equals(dbStatus) && participationDate.isBefore(today)) {
          // 과거 날짜의 결석은 표시
          displayStatus = "ABSENCE";
          displayStatusText = "결석";
          isStatusVisible = true;
        } else {
          // 오늘 날짜이거나 아직 미정인 상태는 숨김
          displayStatus = "PENDING";
          displayStatusText = "";
          isStatusVisible = false;
        }
      }
    }

    participation.setDisplayStatus(displayStatus);
    participation.setDisplayStatusText(displayStatusText);
    participation.setIsStatusVisible(isStatusVisible);

    return participation;
  }



  /**
   * 상태별 텍스트 반환
   */
  private String getStatusText(String status) {
    return switch (status) {
      case "ATTENDANCE" -> "출석";
      case "LATE" -> "지각";
      case "LEAVE_EARLY" -> "조퇴";
      case "VACATION" -> "휴가";
      case "VACATION_PENDING" -> "휴가(미승인)";
      case "ABSENCE" -> "결석";
      case "IN_STUDY" -> "수업중";
      default -> "";
    };
  }


  public static class AttendanceResult {
    private final String status;
    private final Integer trainingTime;

    public AttendanceResult(String status, Integer trainingTime) {
      this.status = status;
      this.trainingTime = trainingTime;
    }

    public String getStatus() { return status; }
    public Integer getTrainingTime() { return trainingTime; }
  }

  @Override
  @Transactional(readOnly = true)
  public CourseVO getCurrentCourseByUserId(Integer userId) {
    return participationMapper.selectCurrentCourseByUserId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CourseVO> getPreviousCoursesByUserId(Integer userId) {
    return participationMapper.selectPreviousCoursesByUserId(userId);
  }

  /**
   * 이전 날짜의 미완료 출결 기록들을 결석 처리
   * 입실했지만 퇴실하지 않은 기록들을 자동으로 결석 처리함
   */
  @Override
  @Transactional
  public void processIncompleteRecordsFromPreviousDays(LocalDate currentDate) {
    log.info("이전 날짜 미완료 출결 기록 처리 시작: {} 이전", currentDate);

    try {
      // ✅ 수정: LocalDate를 String으로 변환하여 전달
      String currentDateStr = currentDate.toString(); // "2025-06-25" 형식

      // 입실했지만 퇴실하지 않은 기록들 조회
      List<ParticipationVO> incompleteRecords =
          participationMapper.selectIncompleteRecords(currentDateStr); // ✅ String으로 전달

      log.info("미완료 출결 기록 발견: {}건", incompleteRecords.size());

      int processedCount = 0;
      for (ParticipationVO record : incompleteRecords) {
        try {
          // 결석 처리로 업데이트
          ParticipationDTO updateDto = ParticipationDTO.builder()
              .id(record.getId())
              .learnerEnrollmentId(record.getLearnerEnrollmentId())
              .status("ABSENCE") // 결석 처리
              .checkIn(record.getCheckIn()) // 입실 시간은 유지
              .checkOut(null)  // 퇴실 시간은 null 유지
              .trainingTime(0)  // 인정시간 0
              .participationDate(record.getParticipationDate())
              .build();

          int updateResult = participationMapper.updateParticipation(updateDto);

          if (updateResult > 0) {
            processedCount++;
            log.debug("미완료 기록 결석 처리 완료: participationId={}, learnerEnrollmentId={}, date={}",
                record.getId(), record.getLearnerEnrollmentId(), record.getParticipationDate());
          } else {
            log.warn("미완료 기록 업데이트 실패: participationId={}", record.getId());
          }
        } catch (Exception e) {
          log.error("개별 미완료 기록 처리 중 오류: participationId={}", record.getId(), e);
        }
      }

      log.info("이전 날짜 미완료 출결 기록 처리 완료: 전체 {}건 중 {}건 처리",
          incompleteRecords.size(), processedCount);

    } catch (Exception e) {
      log.error("미완료 출결 기록 처리 중 전체 오류 발생", e);
      throw e; // 스케줄러에서 오류 로그를 볼 수 있도록 재throw
    }
  }



  /**
   * 특정 교육생의 날짜 범위별 출결 데이터 조회
   */
  @Override
  @Transactional(readOnly = true)
  public List<ParticipationVO> getParticipationByLearnerEnrollmentIdAndDateRange(
      Integer learnerEnrollmentId, LocalDate startDate, LocalDate endDate) {
    return participationMapper.selectByLearnerEnrollmentIdAndDateRange(learnerEnrollmentId, startDate, endDate)
        .stream()
        .map(this::applyDisplayStatus)
        .collect(Collectors.toList());
  }


  /**
   * 어제까지의 출결 통계 조회
   * 스케줄러로 생성된 오늘의 기본 결석 상태는 포함하지 않음
   */
  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getAttendanceStatsUntilYesterday(Integer learnerEnrollmentId) {
    try {
      LocalDate yesterday = LocalDate.now().minusDays(1);
      String yesterdayStr = yesterday.toString();

      // 어제까지의 출결 통계 조회
      Map<String, Object> stats = participationMapper.selectAttendanceStatsUntilDate(learnerEnrollmentId, yesterdayStr);

      // null 값 처리
      stats.put("attendanceCount", stats.getOrDefault("attendanceCount", 0));
      stats.put("lateCount", stats.getOrDefault("lateCount", 0));
      stats.put("earlyCount", stats.getOrDefault("earlyCount", 0));
      stats.put("absenceCount", stats.getOrDefault("absenceCount", 0));
      stats.put("vacationCount", stats.getOrDefault("vacationCount", 0));

      log.debug("어제까지 출결 통계 조회 완료: learnerEnrollmentId={}, 기준일={}, stats={}",
          learnerEnrollmentId, yesterday, stats);
      return stats;
    } catch (Exception e) {
      log.error("어제까지 출결 통계 조회 중 오류: learnerEnrollmentId={}", learnerEnrollmentId, e);
      return Map.of(
          "attendanceCount", 0,
          "lateCount", 0,
          "earlyCount", 0,
          "absenceCount", 0,
          "vacationCount", 0
      );
    }
  }

  /**
   * 사용자 ID와 과정 ID로 learnerEnrollmentId 조회
   */
  @Override
  @Transactional(readOnly = true)
  public Integer getLearnerEnrollmentIdByUserIdAndCourseId(Integer userId, Integer courseId) {
    try {
      Integer learnerEnrollmentId = participationMapper.selectLearnerEnrollmentIdByUserIdAndCourseId(userId, courseId);
      log.debug("learnerEnrollmentId 조회 - userId: {}, courseId: {}, result: {}",
          userId, courseId, learnerEnrollmentId);
      return learnerEnrollmentId;
    } catch (Exception e) {
      log.error("learnerEnrollmentId 조회 중 오류 - userId: {}, courseId: {}", userId, courseId, e);
      return null;
    }
  }

  /**
   * 휴가 신청 가능한 날짜 목록 조회
   */
  @Override
  @Transactional(readOnly = true)
  public List<LocalDate> getAvailableVacationDates(Integer learnerEnrollmentId) {
    try {
      log.debug("휴가 신청 가능 날짜 조회 시작 - learnerEnrollmentId: {}", learnerEnrollmentId);

      // 1. 과정 정보 조회 (종료일 확인용)
      CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
      if (course == null) {
        log.warn("과정 정보를 찾을 수 없음 - learnerEnrollmentId: {}", learnerEnrollmentId);
        return List.of();
      }

      LocalDate today = LocalDate.now();
      LocalDate courseEndDate = course.getEndDate();

      log.debug("과정 종료일: {}, 오늘: {}", courseEndDate, today);

      // 2. course_schedule에서 해당 과정의 수업일 목록 조회 (오늘 이후)
      List<LocalDate> scheduledDates = participationMapper.selectScheduledDatesForCourse(
          course.getId(), today, courseEndDate);

      // 3. 이미 출결 기록이 있는 날짜 제외
      List<LocalDate> excludeDates = participationMapper.selectExistingParticipationDates(
          learnerEnrollmentId, today, courseEndDate);

      // 4. 최종 가능한 날짜 필터링
      List<LocalDate> availableDates = scheduledDates.stream()
          .filter(date -> !excludeDates.contains(date))
          .sorted()
          .collect(Collectors.toList());

      log.debug("휴가 신청 가능 날짜 조회 완료 - 전체: {}개, 가능: {}개",
          scheduledDates.size(), availableDates.size());

      return availableDates;
    } catch (Exception e) {
      log.error("휴가 신청 가능 날짜 조회 중 오류 - learnerEnrollmentId: {}", learnerEnrollmentId, e);
      return List.of();
    }
  }


  /**
   * 어제까지의 과정 진행률 조회 (실제 진행일수 기준)
   * 과정 시작일부터 어제까지 실제로 진행된 수업일수 / 총 수업일수 × 100
   * 출결 상태와 관계없이 순수하게 "과정이 몇일째 진행되었는가"를 보여줌
   */
  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getCourseProgressUntilYesterday(Integer learnerEnrollmentId) {
    try {
      LocalDate yesterday = LocalDate.now().minusDays(1);
      String yesterdayStr = yesterday.toString();

      // 1. 총 수업일수 조회 (course_schedule 기반)
      Integer totalDays = participationMapper.selectTotalCourseDaysByLearnerEnrollmentId(learnerEnrollmentId);
      if (totalDays == null) totalDays = 0;

      // 2. ✅ 새로운 계산: 과정 시작일부터 어제까지 실제 진행된 수업일수
      Integer progressedDays = participationMapper.selectProgressedDaysUntilDate(learnerEnrollmentId, yesterdayStr);
      if (progressedDays == null) progressedDays = 0;

      // 3. 과정 진행률 계산 (실제 진행일수 기준)
      double progressRate = totalDays > 0 ? (double) progressedDays / totalDays * 100 : 0.0;
      progressRate = Math.round(progressRate * 10.0) / 10.0; // 소수점 첫째자리까지

      Map<String, Object> progress = Map.of(
          "totalDays", totalDays,                    // 총 수업일수
          "progressedDays", progressedDays,          // ✅ 실제 진행일수
          "progressRate", progressRate,              // 과정 진행률 (%)
          "baseDate", yesterday                      // 기준 날짜 (어제)
      );

      log.debug("어제까지 과정 진행률 조회 완료: learnerEnrollmentId={}, 진행일수={}/{}, 진행률={}%, 기준일={}",
          learnerEnrollmentId, progressedDays, totalDays, progressRate, yesterday);
      return progress;
    } catch (Exception e) {
      log.error("어제까지 과정 진행률 조회 중 오류: learnerEnrollmentId={}", learnerEnrollmentId, e);
      return Map.of(
          "totalDays", 0,
          "progressedDays", 0,
          "progressRate", 0.0,
          "baseDate", LocalDate.now().minusDays(1)
      );
    }
  }


}
