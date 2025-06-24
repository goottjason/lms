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
    if (!isClassDay(participationDate)) {
      log.info("오늘은 수업이 없는 날입니다: {}", participationDate);
      return;
    }

    // course_schedule에서 해당 날짜에 수업이 있는 과정들 조회
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
  public boolean isClassDay(LocalDate date) {
    List<Integer> courseIds = participationCourseMapper.selectCoursesBySchedule(date);
    return !courseIds.isEmpty();
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
   * 입실 처리 (지각 여부 판단 후 적절한 상태 설정)
   */
  @Override
  public boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() != null) {
      return false;
    }

    // ✅ 수정: 과정 정보 조회하여 지각 여부 판단
    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(learnerEnrollmentId);
    if (course == null) {
      return false;
    }

    // ✅ 수정: 지각 여부 판단
    LocalTime checkInTimeOnly = checkInTime.toLocalTime();
    LocalTime lessonStartTime = course.getLessonStartTime();
    boolean isLate = checkInTimeOnly.isAfter(lessonStartTime);

    // ✅ 수정: 입실 시 상태 결정 (지각이면 LATE, 아니면 IN_STUDY)
    String initialStatus = isLate ? "LATE" : "IN_STUDY";

    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status(initialStatus)  // ✅ 수정: 유동적 상태 설정
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
   * 퇴실 예상 상태 예측
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

    // lesson_end_time, lesson_end_time+10 계산
    LocalTime lessonEnd = course.getLessonEndTime();
    LocalTime lessonEndPlus10 = lessonEnd.plusMinutes(10);
    LocalTime nowTime = predictedCheckOut.toLocalTime();
    boolean isLate = "LATE".equals(participation.getStatus());

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

    if (actualHours >= 8) {
      return "ATTENDANCE";
    } else if (actualHours >= 4) {
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

  // Private 메서드들
  private AttendanceResult calculateFinalAttendanceStatus(LocalDateTime checkIn, LocalDateTime checkOut, CourseVO course) {
    if (checkIn == null || checkOut == null || course == null) {
      return new AttendanceResult("ABSENCE", 0);
    }

    long actualMinutes = TimeCalculationUtil.calculateActualStudyMinutes(
        checkIn, checkOut, course.getLunchStartTime(), course.getLunchEndTime());
    long actualHours = actualMinutes / 60;
    boolean isLate = checkIn.toLocalTime().isAfter(course.getLessonStartTime());
    String status = TimeCalculationUtil.determineAttendanceStatus(actualHours, isLate);
    int trainingTime = TimeCalculationUtil.calculateTrainingTime(status);

    return new AttendanceResult(status, trainingTime);
  }

  private ParticipationVO applyDisplayStatus(ParticipationVO participation) {
    String dbStatus = participation.getStatus();
    String displayStatus;
    String displayStatusText;
    boolean isStatusVisible;

    if ("VACATION".equals(dbStatus)) {
      displayStatus = "VACATION";
      displayStatusText = "휴가";
      isStatusVisible = true;
    } else if ("VACATION_PENDING".equals(dbStatus)) {
      displayStatus = "휴가_미승인";
      displayStatusText = "";
      isStatusVisible = false;
    } else if (participation.getCheckOut() != null && !"ABSENCE".equals(dbStatus) && !"VACATION_PENDING".equals(dbStatus)) {
      displayStatus = dbStatus;
      displayStatusText = getStatusText(dbStatus);
      isStatusVisible = true;
    } else if (participation.getCheckIn() != null && participation.getCheckOut() == null) {
      displayStatus = "IN_STUDY";
      displayStatusText = "수업중";
      isStatusVisible = true;
    } else {
      displayStatus = "PENDING";
      displayStatusText = "";
      isStatusVisible = false;
    }

    participation.setDisplayStatus(displayStatus);
    participation.setDisplayStatusText(displayStatusText);
    participation.setIsStatusVisible(isStatusVisible);
    return participation;
  }

  private String getStatusText(String status) {
    return switch (status) {
      case "ATTENDANCE" -> "출석";
      case "LATE" -> "지각";
      case "LEAVE_EARLY" -> "조퇴";
      case "VACATION" -> "휴가";
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

}
