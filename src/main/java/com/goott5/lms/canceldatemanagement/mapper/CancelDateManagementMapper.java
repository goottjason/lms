package com.goott5.lms.canceldatemanagement.mapper;

import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import com.goott5.lms.canceldatemanagement.domain.ScheduleDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

  @Delete("delete from cancel_date where id = #{id}")
  void deleteCancelDate(Integer id);

  void insertCancelDate(CancelDateDTO cancelDateDTO);

  List<CourseVO> selectCoursesByInProgress(Integer inProgressType);

  @Select("select id, name from course where id not in (select c.id from course c join cancel_date cc on c.id = cc.course_id where cc.cancel_date = #{cancelDate}) and is_in_progress = 1 and #{cancelDate} between start_date and end_date")
  List<CourseVO> selectCoursesInProgressByDate(@Param("cancelDate") LocalDate cancelDate);

  @Select("select id, cancel_date from cancel_date where is_all = 1")
  List<CancelDateVO> selectCancelDatesByIsAll();

  @Select("select class_date from course_schedule where course_id = #{courseId} group by class_date order by class_date")
  List<LocalDate> selectClassDates(CancelDateDTO cancelDateDTO);

  @Select("select subject_id, course_id, class_date, period, period_start_time, period_end_time from course_schedule where course_id = #{courseId} and class_date >= #{cancelDate} order by class_date")
  List<ScheduleDTO> selectRemainSchedules(CancelDateDTO cancelDateDTO);

  @Delete("delete from course_schedule where course_id = #{courseId} and class_date >= #{cancelDate}")
  void deleteRemainSchedules(CancelDateDTO cancelDateDTO);

  void insertNewSchedules(List<ScheduleDTO> scheduleDTOS);

  @Update("update course set end_date = #{newLastDate} where id =#{courseId}")
  void updateCourseEndDate(@Param("courseId") int courseId,
          @Param("newLastDate") LocalDate newLastDate);

  @Select("select is_all, course_id, is_public_holiday, cancel_date, reason from cancel_date where id = #{id}")
  CancelDateDTO selectCancelDateForDelete(Integer id);

  @Select("select class_date from course_schedule where course_id = #{courseId} and class_date > #{cancelDate} group by class_date order by class_date")
  List<LocalDate> selectRemainClassDates(CancelDateDTO cancelDateDTO);

  @Delete("delete from cancel_date where cancel_date = #{cancelDate}")
  void deleteAllCancelDateByDate(LocalDate cancelDate);
}
