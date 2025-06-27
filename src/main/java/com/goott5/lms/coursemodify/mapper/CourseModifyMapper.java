package com.goott5.lms.coursemodify.mapper;

import com.goott5.lms.coursemodify.domain.CourseModifyDTO;
import com.goott5.lms.coursemodify.domain.CourseResponseDTO;
import com.goott5.lms.coursemodify.domain.SubjectVO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourseModifyMapper {


  @Select("select c.id, c.is_in_progress, c.name, c.number_of_learner, c.start_date, c.end_date, c.total_hours, c.total_days, c.daily_hours, c.break_time, c.lesson_start_time, c.lesson_end_time, c.lunch_start_time, c.lunch_end_time, inst.fullname as fulltimeInstructorFullname, inst.id as instructorId, admin.fullname as courseHeadFullname, admin.id as administratorId , cr.name as classroomName, cr.id as classroomId \n"
          + "from course c join staff_assignment sa_inst on c.id = sa_inst.course_id join user inst on sa_inst.user_id = inst.id and inst.type = 'INSTRUCTOR' join staff_assignment sa_admin on c.id = sa_admin.course_id join user admin on sa_admin.user_id = admin.id and admin.type = 'ADMINISTRATOR' join classroom_allocation ca on ca.course_id = c.id join classroom cr on cr.id = ca.classroom_id where c.id = #{courseId}")
  CourseResponseDTO selectCourseDetailByCourseId(Integer courseId);

  @Select("select subject_order, name, hours, textbook_name, textbook_author from course_subject where course_id = #{courseId}")
  List<SubjectVO> selectSubjectsByCourseId(Integer courseId);

  @Update("update course set name = #{name}, number_of_learner = #{numberOfLearner}, start_date = #{startDate}, end_date = #{endDate}, total_hours = #{totalHours}, total_days = #{totalDays}, daily_hours = #{dailyHours}, break_time = #{breakTime}, lesson_start_time = #{lessonStartTime}, lesson_end_time = #{lessonEndTime}, lunch_start_time = #{lunchStartTime}, lunch_end_time = #{lunchEndTime} where id = #{id}")
  int updateCourse(CourseModifyDTO courseModifyDTO);

  @Update("update staff_assignment set user_id = #{instructorId} where course_id = #{id} and user_id in (select id from user u where type = 'INSTRUCTOR')")
  int updateInstructor(CourseModifyDTO courseModifyDTO);

  @Update("update staff_assignment set user_id = #{administratorId} where course_id = #{id} and user_id in (select id from user u where type = 'ADMINISTRATOR')")
  int updateAdministrator(CourseModifyDTO courseModifyDTO);

  @Update("update classroom_allocation set classroom_id = #{classroomId} where course_id = #{id}")
  int updateClassroomAllocation(CourseModifyDTO courseModifyDTO);

  @Update("update classroom set is_active = 0 where id = (select classroom_id from classroom_allocation where course_id = #{id})")
  int updateOldClassRoom(CourseModifyDTO courseModifyDTO);

  @Update("update classroom set is_active = 1 where id = #{classroomId}")
  int updateNewClassroom(CourseModifyDTO courseModifyDTO);

  @Delete("delete from course_schedule where course_id = #{id}")
  int deleteSchedule(CourseModifyDTO courseModifyDTO);

  @Delete("delete from course_subject where course_id = #{id}")
  int deleteSubject(CourseModifyDTO courseModifyDTO);
}
