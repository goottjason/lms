package com.goott5.lms.canceldatemanagement.mapper;

import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CancelDateManagementMapper {

  @Insert("insert into cancel_date(is_all, is_public_holiday, cancel_date, reason) values(#{isAll}, #{isPublicHoliday}, #{cancelDate}, #{reason})")
  int insertHoliday(CancelDateDTO cancelDateDTO);

  @Select("select id, is_all, course_id, is_public_holiday, cancel_date, reason, created_at, updated_at, deleted_at from cancel_date where year(cancel_date) >= year(now()) and is_public_holiday = 1")
  List<CancelDateDTO> selectNowAndNextYearHolidays();

  List<CancelDateVO> selectCancelDates(PagingRequestDTO pagingRequestDTO);

  int selectCountOfCancelDate(PagingRequestDTO pagingRequestDTO);

  @Update("update cancel_date set deleted_at = now() where id = #{id}")
  void updateDeletedAtOfCancelDate(Integer id);

  void insertCancelDate(CancelDateDTO cancelDateDTO);

  List<CourseVO> selectCoursesByInProgress(Integer inProgressType);
}
