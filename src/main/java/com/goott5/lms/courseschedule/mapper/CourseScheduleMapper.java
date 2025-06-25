package com.goott5.lms.courseschedule.mapper;

import com.goott5.lms.courseschedule.domain.CourseVO;
import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.domain.ScheduleVO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseScheduleMapper {

  List<CourseVO> selectCoursesByInProgressAndLoginUser(@Param("inProgressType") Integer inProgressType, @Param("loginUser") UserVO loginUser);

  @Select("select sd.position from user u join staff_detail sd on u.id = sd.user_id where u.id = #{id}")
  String selectUserPosition(UserVO loginUser);

  List<CourseVO> selectCoursesByInProgress(Integer inProgressType);

  @Select("select id, is_in_progress, name, number_of_learner, start_date, end_date, total_hours, total_days, daily_hours, break_time, lesson_start_time, lesson_end_time, lunch_start_time, lunch_end_time from course order by end_date desc limit 1 ")
  CourseVO selectFirstCourseInAllCourses();


  CourseVO selectFirstCourseByUser(UserVO loginUser);

  @Select("select cs.id, cs.course_id, cs.class_date, cs.period, cs.period_start_time, cs.period_end_time, csub.name as subject_name, u.fullname as instructor_name, cl.name as classroom_name from course_schedule cs join staff_assignment sa on cs.course_id = sa.course_id join user u on sa.user_id = u.id join classroom_allocation ca on cs.course_id = ca.course_id join classroom cl on cl.id = ca.classroom_id join course_subject csub on cs.subject_id = csub.id  where cs.course_id = #{courseId} and u.type = 'INSTRUCTOR' and cs.class_date between #{weekStart} and #{weekEnd} order by cs.class_date, cs.period")
  List<ScheduleVO> selectCourseSchedulesByWeek(ScheduleRequestDTO scheduleRequestDTO);

  List<CourseVO> selectcoursesByUser(UserVO loginUser);

  @Select("select id, is_in_progress, name, number_of_learner, start_date, end_date, total_hours, total_days, daily_hours, break_time, lesson_start_time, lesson_end_time, lunch_start_time, lunch_end_time from course where id = #{courseId}")
  CourseVO selectCourseById(int courseId);

  @Select("select c.id, c.is_in_progress, c.name, c.number_of_learner, c.start_date, c.end_date, c.total_hours, c.total_days, c.daily_hours, c.break_time, c.lesson_start_time, c.lesson_end_time, c.lunch_start_time, c.lunch_end_time from course c join staff_assignment sa on c.id = sa.course_id where sa.course_id = #{courseId} and sa.user_id = #{loginUser.id}")
  CourseVO selectCourseByIdAndUser(@Param("courseId") int courseId, @Param("loginUser") UserVO loginUser);
}
