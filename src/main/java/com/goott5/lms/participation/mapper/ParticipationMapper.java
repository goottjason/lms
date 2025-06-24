package com.goott5.lms.participation.mapper;

import com.goott5.lms.participation.domain.CourseVO;
import com.goott5.lms.participation.domain.ParticipationDTO;
import com.goott5.lms.participation.domain.ParticipationVO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

  // 특정 과정의 수강 중인 교육생 id 목록 조회
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

}
