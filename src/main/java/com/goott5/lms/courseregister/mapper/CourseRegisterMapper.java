package com.goott5.lms.courseregister.mapper;

import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.courseregister.domain.ClassroomVO;
import com.goott5.lms.courseregister.domain.CourseSaveDTO;
import com.goott5.lms.courseregister.domain.ScheduleDTO;
import com.goott5.lms.courseregister.domain.SubjectDTO;
import com.goott5.lms.courseregister.domain.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourseRegisterMapper {


  @Select("select id, fullname from user where type = 'INSTRUCTOR' and id "
          + "not in (select u.id from staff_assignment sa left join course c on sa.course_id = c.id  left join user u on sa.user_id = u.id "
          + "where c.is_in_progress = 1 and u.type = 'INSTRUCTOR')")
  List<UserVO> selectNotAssignmentInstructor();

  @Select("select u.id, u.fullname from user u join staff_detail sd on u.id = sd.user_id where position = 'course_head'")
  List<UserVO> selectCourseHead();

  @Select("select id, is_all, course_id, is_public_holiday, cancel_date, reason, created_at, updated_at, deleted_at from cancel_date where is_all = 1")
  List<CancelDateVO> selectCancelDates();

  @Select("select id, name from classroom where is_active = 0 and deleted_at is null")
  List<ClassroomVO> selectClassrooms();

  @Insert("insert into course(name, number_of_learner, start_date, end_date, total_hours, total_days, daily_hours, break_time, lesson_start_time, lesson_end_time, lunch_start_time, lunch_end_time) values(#{name}, #{numberOfLearner}, #{startDate}, #{endDate}, #{totalHours}, #{totalDays}, #{dailyHours}, #{breakTime}, #{lessonStartTime}, #{lessonEndTime}, #{lunchStartTime}, #{lunchEndTime})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertCourse(CourseSaveDTO courseSaveDTO);

  @Insert("insert into staff_assignment(course_id, user_id) values(#{id}, #{instructorId}), (#{id}, #{administratorId})")
  void insertStaffAssignment(CourseSaveDTO courseSaveDTO);

  @Insert("insert into classroom_allocation(course_id, classroom_id) values(#{id}, #{classroomId})")
  void insertClassroomAllocation(CourseSaveDTO courseSaveDTO);

  @Update("update classroom set is_active = 1 where id = #{classroomId}")
  void updateClassroom(CourseSaveDTO courseSaveDTO);

  @Insert("insert into course_subject(course_id, subject_order, name, hours, textbook_name, textbook_author) values(#{course_id}, #{subjectOrder}, #{name}, #{hours}, #{textbookName}, #{textbookAuthor})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertCourseSubject(SubjectDTO subjectDTO);

  @Insert("insert into course_schedule(subject_id, course_id, class_date, period, period_start_time, period_end_time) values(#{subjectId}, #{courseId}, #{classDate}, #{period}, #{periodStartTime}, #{periodEndTime})")
  void insertCourseSchedule(ScheduleDTO scheduleDTO);
}
