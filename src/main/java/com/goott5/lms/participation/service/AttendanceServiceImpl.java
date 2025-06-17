package com.goott5.lms.participation.service;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.CancelDateMapper;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.mapper.ParticipationMapper;
import com.goott5.lms.participation.util.TimeCalculationUtil;
import java.time.DayOfWeek;
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
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

  private final ParticipationMapper participationMapper;
  private final ParticipationCourseMapper participationCourseMapper;
  private final CancelDateMapper cancelDateMapper;

  /**
   * 모든 과정의 출결 기록 생성 (스케줄러)
   */
  @Override
  public void createDailyAttendanceForAllCourses(LocalDate participationDate) {
    if (!isClassDay(participationDate)) {
      return;
    }
    List<Integer> courseIds = participationMapper.selectActiveEnrolledCourseIds();
    for (Integer courseId : courseIds) {
      createDailyAttendanceForCourse(courseId, participationDate);
    }
  }

  /**
   * 특정 과정의 출결 기록 생성
   */
  @Override
  public int createDailyAttendanceForCourse(Integer courseId, LocalDate participationDate) {
    if (cancelDateMapper.isCancelDateForCourse(courseId, participationDate)) {
      return 0;
    }
    List<Integer> learnerEnrollmentIds = participationMapper.selectActiveLearnerEnrollmentIdsByCourse(
        courseId);
    int createdCount = 0;
    for (Integer learnerEnrollmentId : learnerEnrollmentIds) {
      if (!participationMapper.existsParticipationToday(learnerEnrollmentId, participationDate)) {
        ParticipationDTO dto = ParticipationDTO.builder()
            .learnerEnrollmentId(learnerEnrollmentId)
            .status("ABSENCE")
            .trainingTime(0)
            .participationDate(participationDate)
            .build();
        participationMapper.insertParticipation(dto);
        createdCount++;
      }
    }
    return createdCount;
  }

  /**
   * 입실 처리 (입실 시간만 기록, 상태는 퇴실 후 결정)
   */
  @Override
  public boolean processCheckIn(Integer learnerEnrollmentId, LocalDateTime checkInTime,
      LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() != null) {
      return false;
    }
    ParticipationDTO updateDto = ParticipationDTO.builder()
        .id(participation.getId())
        .learnerEnrollmentId(participation.getLearnerEnrollmentId())
        .status("ABSENCE")
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
  public boolean processCheckOut(Integer learnerEnrollmentId, LocalDateTime checkOutTime,
      LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() == null
        || participation.getCheckOut() != null) {
      return false;
    }
    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(
        learnerEnrollmentId);
    AttendanceResult result = calculateFinalAttendanceStatus(participation.getCheckIn(),
        checkOutTime, course);
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
  public ParticipationVO getTodayParticipationWithDisplayStatus(Integer learnerEnrollmentId,
      LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
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
  public List<ParticipationVO> getParticipationByDateWithDisplayStatus(
      LocalDate participationDate) {
    return participationMapper.selectParticipationByDate(participationDate)
        .stream().map(this::applyDisplayStatus).collect(Collectors.toList());
  }

  /**
   * 퇴실 예상 상태 예측 (모달창 등)
   */
  @Override
  @Transactional(readOnly = true)
  public String predictAttendanceStatus(Integer learnerEnrollmentId,
      LocalDateTime predictedCheckOut, LocalDate participationDate) {
    ParticipationVO participation = participationMapper.selectByLearnerEnrollmentIdAndDate(
        learnerEnrollmentId, participationDate);
    if (participation == null || participation.getCheckIn() == null) {
      return "ABSENCE";
    }

    CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(
        learnerEnrollmentId);
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
        course.getLunchStartTime(), course.getLunchEndTime()
    );
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
   * 전체 휴강/공휴일 여부
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isClassDay(LocalDate date) {
    if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
      return false;
    }
    return !cancelDateMapper.isHolidayOrCancelDate(date);
  }

  /**
   * 특정 과정의 휴강일 여부
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isClassDayForCourse(Integer courseId, LocalDate date) {
    return isClassDay(date) && !cancelDateMapper.isCancelDateForCourse(courseId, date);
  }

  /**
   * 최종 출결 상태 및 인정시간 계산 (점심시간 제외)
   */
  private AttendanceResult calculateFinalAttendanceStatus(LocalDateTime checkIn,
      LocalDateTime checkOut, CourseVO course) {
    if (checkIn == null || checkOut == null || course == null) {
      return new AttendanceResult("ABSENCE", 0);
    }

    // 수정: 매개변수명을 정확한 필드명으로 전달
    long actualMinutes = TimeCalculationUtil.calculateActualStudyMinutes(
        checkIn, checkOut,
        course.getLunchStartTime(),  // ✅ 수정된 매개변수명과 일치
        course.getLunchEndTime()     // ✅ 수정된 매개변수명과 일치
    );

    long actualHours = actualMinutes / 60;
    boolean isLate = checkIn.toLocalTime().isAfter(course.getLessonStartTime());
    String status = TimeCalculationUtil.determineAttendanceStatus(actualHours, isLate);
    int trainingTime = TimeCalculationUtil.calculateTrainingTime(status);

    return new AttendanceResult(status, trainingTime);
  }


  /**
   * 화면 표시 상태 적용 (입실 전/입실 후/퇴실 후/휴가 등)
   */
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
      displayStatus = "PENDING";
      displayStatusText = "";
      isStatusVisible = false;
    } else if (participation.getCheckOut() != null && !"ABSENCE".equals(dbStatus)
        && !"VACATION_PENDING".equals(dbStatus)) {
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

  /**
   * 상태별 한글명 반환
   */
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

  /**
   * 출결 상태/인정시간 결과 DTO
   */
  public static class AttendanceResult {

    private final String status;
    private final Integer trainingTime;

    public AttendanceResult(String status, Integer trainingTime) {
      this.status = status;
      this.trainingTime = trainingTime;
    }

    public String getStatus() {
      return status;
    }

    public Integer getTrainingTime() {
      return trainingTime;
    }
  }

  /**
   * 교육생의 수업 진행률 계산 (%)
   */
  @Override
  @Transactional(readOnly = true)
  public double getProgressPercentage(Integer learnerEnrollmentId) {
    try {
      // 교육생의 총 training_time 합계
      Integer totalTrainingTime = participationMapper.selectTotalTrainingTimeByLearnerEnrollmentId(
          learnerEnrollmentId);
      if (totalTrainingTime == null) {
        totalTrainingTime = 0;
      }

      // 해당 과정의 총 수업시간
      CourseVO course = participationCourseMapper.selectCourseByLearnerEnrollmentId(
          learnerEnrollmentId);
      if (course == null || course.getTotalHours() == null || course.getTotalHours() == 0) {
        return 0.0;
      }

      // 진행률 계산 (소수점 둘째자리까지)
      double percentage = (double) totalTrainingTime / course.getTotalHours() * 100;
      return Math.round(percentage * 100.0) / 100.0;

    } catch (Exception e) {
      log.error("진행률 계산 중 오류 발생: learnerEnrollmentId={}", learnerEnrollmentId, e);
      return 0.0;
    }
  }


}
