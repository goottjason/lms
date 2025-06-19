package com.goott5.lms.participation.mapper;

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

  // 진행률 계산용 메서드 추가
  // 특정 교육생의 총 training_time 합계 조회
  Integer selectTotalTrainingTimeByLearnerEnrollmentId(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId);

  // 새로 추가: 사용자 ID로 learnerEnrollmentId 조회
  Integer selectLearnerEnrollmentIdByUserId(@Param("userId") Integer userId);
}
