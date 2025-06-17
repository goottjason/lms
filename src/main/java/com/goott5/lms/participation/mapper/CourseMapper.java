package com.goott5.lms.participation.mapper;

import com.goott5.lms.participation.domain.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 과정(Course) 정보 조회 매퍼
 */
@Mapper
public interface CourseMapper {

  // 과정 id로 과정 정보 조회
  CourseVO selectCourseById(@Param("id") Integer id);

  // learnerEnrollmentId로 과정 정보 조회
  CourseVO selectCourseByLearnerEnrollmentId(
      @Param("learnerEnrollmentId") Integer learnerEnrollmentId);
}
