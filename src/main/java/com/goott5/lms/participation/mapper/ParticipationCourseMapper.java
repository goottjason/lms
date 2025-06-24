package com.goott5.lms.participation.mapper;

import com.goott5.lms.participation.domain.CourseVO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 과정(Course) 정보 조회 매퍼
 */
@Mapper
public interface ParticipationCourseMapper {

  // 과정 id로 과정 정보 조회
  CourseVO selectCourseById(@Param("id") Integer id);

  // learnerEnrollmentId로 과정 정보 조회
  CourseVO selectCourseByLearnerEnrollmentId(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId);

  // course_schedule 테이블에서 해당 날짜에 수업이 있는 과정 ID들 조회
  @Select("SELECT DISTINCT course_id FROM course_schedule WHERE class_date = #{today}")
  List<Integer> selectCoursesBySchedule(@Param("today") LocalDate today);

  /**
   * 사용자 ID로 현재 수강 중인 learner_enrollment_id 조회
   */
  Integer selectLearnerEnrollmentIdByUserId(@Param("userId") Integer userId);

}
