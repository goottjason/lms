package com.goott5.lms.participation.mapper;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 휴강일/공휴일 관리 매퍼
 */
@Mapper
public interface CancelDateMapper {

  // 전체 휴강/공휴일 여부
  boolean isHolidayOrCancelDate(@Param("date") LocalDate date);

  // 특정 과정의 휴강일 여부
  boolean isCancelDateForCourse(@Param("courseId") Integer courseId, @Param("date") LocalDate date);
}
