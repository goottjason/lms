package com.goott5.lms.participation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.goott5.lms.participation.domain.ParticipationVO;
import com.goott5.lms.participation.service.AttendanceService;
import com.goott5.lms.participation.service.VacationService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class VacationTest {

  @Autowired
  private VacationService vacationService;

  @Autowired
  private AttendanceService attendanceService;

  @Test
  @DisplayName("휴가 신청 및 승인 테스트")
  void testVacationApplyAndApprove() {
    // Given
    Integer learnerEnrollmentId = 17;
    LocalDate vacationDate = LocalDate.now().plusDays(1);
    String explanation = "개인 사유로 인한 휴가 신청";

    System.out.println("=== 휴가 신청 및 승인 테스트 시작 ===");

    // When - 휴가 신청
    boolean applyResult = vacationService.applyVacation(learnerEnrollmentId, vacationDate,
        explanation);
    assertTrue(applyResult, "휴가 신청이 성공해야 함");

    // Then - 신청 후 상태 확인
    ParticipationVO afterApply = attendanceService.getTodayParticipationWithDisplayStatus(
        learnerEnrollmentId, vacationDate);
    assertNotNull(afterApply, "출결 기록이 존재해야 함");
    assertEquals("VACATION_PENDING", afterApply.getStatus(), "DB 상태는 VACATION_PENDING이어야 함");
    assertEquals("PENDING", afterApply.getDisplayStatus(), "화면 표시 상태는 PENDING이어야 함");
    assertFalse(afterApply.getIsStatusVisible(), "사용자에게 보이지 않아야 함");
    assertEquals(0, afterApply.getTrainingTime(), "승인 전까지는 인정시간이 0이어야 함");

    System.out.println("휴가 신청 후 상태:");
    System.out.println("  - DB 상태: " + afterApply.getStatus());
    System.out.println("  - 화면 상태: " + afterApply.getDisplayStatus());
    System.out.println("  - 표시 여부: " + afterApply.getIsStatusVisible());
    System.out.println("  - 인정시간: " + afterApply.getTrainingTime());

    // When - 휴가 승인
    boolean approveResult = vacationService.approveVacation(afterApply.getId());
    assertTrue(approveResult, "휴가 승인이 성공해야 함");

    // Then - 승인 후 상태 확인
    ParticipationVO afterApprove = attendanceService.getTodayParticipationWithDisplayStatus(
        learnerEnrollmentId, vacationDate);
    assertNotNull(afterApprove, "출결 기록이 존재해야 함");
    assertEquals("VACATION", afterApprove.getStatus(), "DB 상태는 VACATION이어야 함");
    assertEquals("VACATION", afterApprove.getDisplayStatus(), "화면 표시 상태는 VACATION이어야 함");
    assertTrue(afterApprove.getIsStatusVisible(), "사용자에게 보여야 함");
    assertEquals("휴가", afterApprove.getDisplayStatusText(), "상태 텍스트가 '휴가'여야 함");
    assertEquals(8, afterApprove.getTrainingTime(), "승인 후 인정시간이 8시간이어야 함");

    System.out.println("휴가 승인 후 상태:");
    System.out.println("  - DB 상태: " + afterApprove.getStatus());
    System.out.println("  - 화면 상태: " + afterApprove.getDisplayStatus());
    System.out.println("  - 표시 여부: " + afterApprove.getIsStatusVisible());
    System.out.println("  - 상태 텍스트: " + afterApprove.getDisplayStatusText());
    System.out.println("  - 인정시간: " + afterApprove.getTrainingTime());

    System.out.println("✅ 휴가 신청 및 승인 테스트 성공!");
  }

  @Test
  @DisplayName("휴가 신청 및 거부 테스트")
  void testVacationApplyAndReject() {
    // Given
    Integer learnerEnrollmentId = 17;
    LocalDate vacationDate = LocalDate.now().plusDays(2);
    String explanation = "가족 행사로 인한 휴가 신청";

    System.out.println("=== 휴가 신청 및 거부 테스트 시작 ===");

    // When - 휴가 신청
    boolean applyResult = vacationService.applyVacation(learnerEnrollmentId, vacationDate,
        explanation);
    assertTrue(applyResult, "휴가 신청이 성공해야 함");

    ParticipationVO afterApply = attendanceService.getTodayParticipationWithDisplayStatus(
        learnerEnrollmentId, vacationDate);
    Integer participationId = afterApply.getId();

    // When - 휴가 거부
    boolean rejectResult = vacationService.rejectVacation(participationId);
    assertTrue(rejectResult, "휴가 거부가 성공해야 함");

    // Then - 거부 후 상태 확인 (기록이 삭제되어야 함)
    ParticipationVO afterReject = attendanceService.getTodayParticipationWithDisplayStatus(
        learnerEnrollmentId, vacationDate);
    assertNull(afterReject, "출결 기록이 삭제되어야 함");

    System.out.println("✅ 휴가 신청 및 거부 테스트 성공!");
  }
}
