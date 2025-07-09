package com.goott5.lms.participation.mapper;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 출결(Participation) 관련 DB 매퍼
 */
@Mapper
public interface ParticipationMapper {

  // 출결 기록 등록
  int insertParticipation(ParticipationDTO dto);

  // 출결 기록 단건 조회 (PK)
  ParticipationVO selectParticipationById(@Param("id") Integer id);

  // 날짜별 출결 기록 전체 조회
  List<ParticipationVO> selectParticipationByDate(
      @Param("participationDate") LocalDate participationDate);

  // 전체 출결 기록 조회
  List<ParticipationVO> selectAllParticipation();

  // 출결 기록 총 개수
  int countAll();

  /**
   * 특정 과정의 현재 수강 중인 교육생 ID 목록 조회
   * COMPLETED(이수), DROPPED(중도탈락) 제외, IN_PROGRESS만 포함
   */
  List<Integer> selectActiveLearnerEnrollmentIdsByCourse(@Param("courseId") Integer courseId);

  // 특정 교육생의 특정 날짜 출결 기록 조회
  ParticipationVO selectByLearnerEnrollmentIdAndDate(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("participationDate") LocalDate participationDate);

  // 출결 기록 수정
  int updateParticipation(ParticipationDTO dto);

  // 오늘 출결 기록 존재 여부
  boolean existsParticipationToday(@Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("participationDate") LocalDate participationDate);

  // 수강 중인 과정 id 목록 조회 (스케줄러용)
  List<Integer> selectActiveEnrolledCourseIds();

  // 하드 딜리트 (완전 삭제) - 휴가용
  void deleteParticipation(Integer id);

  /**
   * 교육생의 출석일수 조회 (training_time > 0인 날의 개수)
   */
  Integer selectAttendanceDaysByLearnerEnrollmentId(@Param("learnerEnrollmentId") Integer learnerEnrollmentId);

  /**
   * 교육생의 과정별 전체 수업일수 조회 (course_schedule 기반)
   */
  Integer selectTotalClassDaysByLearnerEnrollmentId(@Param("learnerEnrollmentId") Integer learnerEnrollmentId);


  /**
   * 사용자의 현재 수강 중인 과정 조회
   */
  CourseVO selectCurrentCourseByUserId(@Param("userId") Integer userId);

  /**
   * 사용자의 이전 수강 과정 목록 조회
   */
  List<CourseVO> selectPreviousCoursesByUserId(@Param("userId") Integer userId);

  /**
   * 특정 교육생의 날짜 범위별 출결 데이터 조회
   */
  List<ParticipationVO> selectByLearnerEnrollmentIdAndDateRange(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  /**
   * 입실했지만 퇴실하지 않은 기록들 조회 (특정 날짜 이전)
   */
  List<ParticipationVO> selectIncompleteRecords(@Param("beforeDate") String beforeDate);

  /**
   * 과정의 총 수업일수 조회 (course_schedule 기반)
   */
  Integer selectTotalCourseDaysByLearnerEnrollmentId(@Param("learnerEnrollmentId") Integer learnerEnrollmentId);

  /**
   * 사용자 ID와 과정 ID로 learnerEnrollmentId 조회
   */
  Integer selectLearnerEnrollmentIdByUserIdAndCourseId(
      @Param("userId") Integer userId,
      @Param("courseId") Integer courseId
  );

  /**
   * 특정 날짜까지의 출결 통계 조회 (해당 날짜 포함)
   */
  Map<String, Object> selectAttendanceStatsUntilDate(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("untilDate") String untilDate
  );

  /**
   * 과정 시작일부터 특정 날짜까지 실제 진행된 수업일수 조회
   */
  Integer selectProgressedDaysUntilDate(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("untilDate") String untilDate
  );

  /**
   * 특정 과정의 수업일 목록 조회 (특정 기간)
   */
  List<LocalDate> selectScheduledDatesForCourse(
      @Param("courseId") Integer courseId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * 이미 출결 기록이 있는 날짜 목록 조회
   */
  List<LocalDate> selectExistingParticipationDates(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * 강사가 맡은 현재 과정 조회
   */
  Map<String, Object> selectCurrentCourseByInstructor(@Param("instructorId") Integer instructorId);

  /**
   * 강사가 맡은 과거 과정 목록 조회
   */
  List<Map<String, Object>> selectPreviousCoursesByInstructor(@Param("instructorId") Integer instructorId);

  /**
   * 과정별 승인 대기 중인 휴가 신청 개수 조회
   */
  int countPendingVacationsByCourse(@Param("courseId") Integer courseId);

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 조회 (페이징)
   */
  List<Map<String, Object>> selectPendingVacationsByCourse(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit
  );

  /**
   * 과정별 승인된 휴가 개수 조회
   */
  int countApprovedVacationsByCourse(@Param("courseId") Integer courseId);

  /**
   * 과정별 승인된 휴가 목록 조회 (페이징)
   */
  List<Map<String, Object>> selectApprovedVacationsByCourseWithPaging(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit
  );

  /**
   * 과정별 모든 휴가 개수 조회 (승인 대기 + 승인된 휴가)
   */
  int countAllVacationsByCourse(@Param("courseId") Integer courseId);

  /**
   * 과정별 모든 휴가 목록 조회 (페이징)
   */
  List<Map<String, Object>> selectAllVacationsByCourseWithPaging(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit
  );

  /**
   * 과정별 승인 대기 중인 휴가 신청 개수 조회 (이름 검색)
   */
  int countPendingVacationsBySearch(@Param("courseId") Integer courseId, @Param("searchName") String searchName);

  /**
   * 과정별 승인 대기 중인 휴가 신청 목록 조회 (이름 검색, 페이징)
   */
  List<Map<String, Object>> selectPendingVacationsBySearch(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit,
      @Param("searchName") String searchName
  );

  /**
   * 과정별 승인된 휴가 개수 조회 (이름 검색)
   */
  int countApprovedVacationsBySearch(@Param("courseId") Integer courseId, @Param("searchName") String searchName);

  /**
   * 과정별 승인된 휴가 목록 조회 (이름 검색, 페이징)
   */
  List<Map<String, Object>> selectApprovedVacationsBySearch(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit,
      @Param("searchName") String searchName
  );

  /**
   * 과정별 모든 휴가 개수 조회 (이름 검색)
   */
  int countAllVacationsBySearch(@Param("courseId") Integer courseId, @Param("searchName") String searchName);

  /**
   * 과정별 모든 휴가 목록 조회 (이름 검색, 페이징)
   */
  List<Map<String, Object>> selectAllVacationsBySearch(
      @Param("courseId") Integer courseId,
      @Param("offset") long offset,
      @Param("limit") int limit,
      @Param("searchName") String searchName
  );


  @Select("select le.user_id from participation p join learner_enrollment le on p.learner_enrollment_id = le.id where p.id = #{participationId}")
  int selectLearnerIdByParticipationId(Integer participationId);

  @Select("select participation_date from participation where id = #{participationId}")
  LocalDate selectVacationDateByParticipationId(Integer participationId);
}
