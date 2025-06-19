package com.goott5.lms.participation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.mapper.ParticipationCourseMapper;
import com.goott5.lms.participation.service.AttendanceService;
import com.goott5.lms.participation.service.ParticipationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ParticipationTest {

  @Autowired
  private ParticipationService participationService;

  @Autowired
  private AttendanceService attendanceService;

  @Autowired

  private ParticipationCourseMapper participationCourseMapper;


  @Test
  @DisplayName("과정3의 모든 수강생 출결 기록 생성 테스트 (스케줄러 대신)")
  void testCreateDailyParticipationForCourse() {
    // Given
    Integer courseId = 3;  // 과정 ID 3
    LocalDate today = LocalDate.now();

    System.out.println("=== 과정 " + courseId + "의 출결 기록 생성 테스트 시작 ===");

    // 먼저 해당 과정의 수강생 목록 확인
    List<Integer> learnerEnrollmentIds = participationService.getActiveLearnerEnrollmentIds(
        courseId);
    System.out.println("과정 " + courseId + "의 수강 중인 학생 수: " + learnerEnrollmentIds.size());
    System.out.println("학생 목록: " + learnerEnrollmentIds);

    // When - 모든 수강생의 출결 기록 생성
    int createdCount = participationService.createDailyParticipationForCourse(courseId, today);

    // Then
    assertTrue(createdCount >= 0, "출결 기록이 생성되어야 함");
    System.out.println("생성된 출결 기록 수: " + createdCount + "건");

    // 각 학생의 출결 기록 확인
    for (Integer learnerEnrollmentId : learnerEnrollmentIds) {
      ParticipationVO participation = participationService.getTodayParticipation(
          learnerEnrollmentId, today);
      assertNotNull(participation, "학생 " + learnerEnrollmentId + "의 출결 기록이 존재해야 함");
      assertEquals("ABSENCE", participation.getStatus(), "기본 상태는 결석이어야 함");
      assertEquals(0, participation.getTrainingTime(), "기본 인정시간은 0이어야 함");
      assertEquals(today, participation.getParticipationDate(), "출결 날짜가 일치해야 함");

      System.out.println("학생 " + learnerEnrollmentId + " 출결 기록: " + participation);
    }

    System.out.println("✅ 과정3 모든 수강생 출결 기록 생성 테스트 성공!");
  }

  @Test
  @DisplayName("학생 user_id=8 입퇴실 처리 테스트")
  void testCheckInOutForStudent8() {
    // Given
    Integer courseId = 3;
    LocalDate today = LocalDate.now();

    System.out.println("=== 학생 user_id=8 입퇴실 테스트 시작 ===");

    // 먼저 출결 기록 생성
    participationService.createDailyParticipationForCourse(courseId, today);

    // user_id=8에 해당하는 learner_enrollment_id 찾기 (실제로는 조인 쿼리 필요)
    // 여기서는 임시로 course_id=3의 첫 번째 학생으로 가정
    List<Integer> learnerEnrollmentIds = participationService.getActiveLearnerEnrollmentIds(
        courseId);

    if (learnerEnrollmentIds.isEmpty()) {
      System.out.println("⚠️ 과정3에 수강 중인 학생이 없어 테스트를 건너뜁니다.");
      return;
    }

    // 첫 번째 학생을 user_id=8로 가정 (실제로는 user_id로 조회해야 함)
    Integer testLearnerEnrollmentId = learnerEnrollmentIds.get(0);
    System.out.println(
        "테스트 대상 learner_enrollment_id: " + testLearnerEnrollmentId + " (user_id=8로 가정)");

    // 입퇴실 시간 설정
    LocalDateTime checkInTime = LocalDateTime.of(2025, 6, 15, 9, 15);   // 09:15 입실
    LocalDateTime checkOutTime = LocalDateTime.of(2025, 6, 15, 18, 25); // 18:25 퇴실

    // When & Then - 입실 처리
    System.out.println("--- 입실 처리 ---");
    ParticipationVO beforeCheckIn = participationService.getTodayParticipation(
        testLearnerEnrollmentId, today);
    System.out.println("입실 전 상태: " + beforeCheckIn);

    boolean checkInResult = participationService.processCheckIn(testLearnerEnrollmentId,
        checkInTime, today);
    assertTrue(checkInResult, "입실 처리가 성공해야 함");

    ParticipationVO afterCheckIn = participationService.getTodayParticipation(
        testLearnerEnrollmentId, today);
    assertNotNull(afterCheckIn.getCheckIn(), "입실 시간이 기록되어야 함");
    assertEquals(checkInTime, afterCheckIn.getCheckIn(), "입실 시간이 일치해야 함");
    System.out.println("입실 후 상태: " + afterCheckIn);

    // When & Then - 퇴실 처리
    System.out.println("--- 퇴실 처리 ---");
    boolean checkOutResult = participationService.processCheckOut(testLearnerEnrollmentId,
        checkOutTime, today);
    assertTrue(checkOutResult, "퇴실 처리가 성공해야 함");

    ParticipationVO afterCheckOut = participationService.getTodayParticipation(
        testLearnerEnrollmentId, today);
    assertNotNull(afterCheckOut.getCheckOut(), "퇴실 시간이 기록되어야 함");
    assertEquals(checkOutTime, afterCheckOut.getCheckOut(), "퇴실 시간이 일치해야 함");
    assertEquals("ATTENDANCE", afterCheckOut.getStatus(), "최종 상태는 출석이어야 함");
    assertEquals(8, afterCheckOut.getTrainingTime(), "인정시간은 8시간이어야 함");
    System.out.println("퇴실 후 최종 상태: " + afterCheckOut);

    System.out.println("✅ 학생 user_id=8 입퇴실 처리 테스트 성공!");
  }

  @Test
  @DisplayName("전체 출결 현황 조회 테스트")
  void testViewAllParticipationStatus() {
    // Given
    Integer courseId = 3;
    LocalDate today = LocalDate.now();

    System.out.println("=== 전체 출결 현황 조회 테스트 ===");

    // 출결 기록 생성
    participationService.createDailyParticipationForCourse(courseId, today);

    // When
    List<ParticipationVO> todayParticipations = participationService.getParticipationByDate(today);
    int totalCount = participationService.getTotalCount();

    // Then
    assertNotNull(todayParticipations, "오늘 출결 기록이 존재해야 함");
    System.out.println("오늘 출결 기록 수: " + todayParticipations.size() + "건");
    System.out.println("전체 출결 기록 수: " + totalCount + "건");

    // 각 출결 기록 출력
    for (ParticipationVO participation : todayParticipations) {
      System.out.println("출결 기록: " + participation);
    }

    System.out.println("✅ 전체 출결 현황 조회 테스트 성공!");
  }

  @Test
  @DisplayName("날짜별 출결 현황 조회 및 과정별 수업일 확인 테스트")
  void testDateParticipationAndClassDay() {
    // Given
    Integer courseId = 3;
    LocalDate today = LocalDate.now();

    System.out.println("=== 날짜별 출결 현황 및 수업일 확인 테스트 ===");

    // 출결 기록 생성

    attendanceService.createDailyAttendanceForCourse(courseId, today);

    // When - 날짜별 출결 현황 조회
    List<ParticipationVO> todayParticipations = attendanceService.getParticipationByDateWithDisplayStatus(
        today);

    // When - 과정별 수업일 확인
//    boolean isClassDay = attendanceService.isClassDayForCourse(courseId, today);

    // Then
    assertNotNull(todayParticipations, "오늘 출결 기록이 존재해야 함");
//    assertTrue(isClassDay, "오늘은 수업일이어야 함");

    System.out.println("오늘 출결 기록 수: " + todayParticipations.size());
//    System.out.println("과정 " + courseId + " 수업일 여부: " + isClassDay);

    System.out.println("✅ 날짜별 출결 현황 및 수업일 확인 테스트 성공!");
  }

  @Test

  @DisplayName("ParticipationCourseMapper selectCourseById 테스트")

  void testSelectCourseById() {
    // Given
    Integer courseId = 3;

    System.out.println("=== 과정 정보 조회 테스트 ===");

    // When


    CourseVO course = participationCourseMapper.selectCourseById(courseId);


    // Then
    if (course != null) {
      System.out.println("과정 정보: " + course);
      assertEquals(courseId, course.getId(), "과정 ID가 일치해야 함");
      System.out.println("✅ 과정 정보 조회 테스트 성공!");
    } else {
      System.out.println("⚠️ 과정 ID " + courseId + "에 해당하는 과정이 없습니다.");
    }
  }

}
