package com.goott5.lms.instructorhome.mapper;

import com.goott5.lms.instructorhome.domain.CourseVO;
import com.goott5.lms.instructorhome.domain.CustomTestDTO;
import com.goott5.lms.instructorhome.domain.CustomTestSubmissionDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InstructorHomeMapper {

  @Select("select id, is_in_progress, name, number_of_learner, start_date, end_date, total_hours, total_days, daily_hours, break_time, lesson_start_time, lesson_end_time, lunch_start_time, lunch_end_time from course where id = #{courseId}")
  CourseVO selectCourseById(Integer courseId);

  @Select("select count(distinct class_date) from course_schedule where class_date < #{now} and course_id = #{courseId}")
  int selectCountOfCompletedDays(@Param("courseId") int courseId, @Param("now") LocalDate now);

  @Select("select count(p.id) from participation p join learner_enrollment le on p.learner_enrollment_id = le.id and le.course_id = #{courseId} where p.participation_date = current_date() and p.status in ('ABSENCE', 'VACATION_PENDING')")
  int selectCountOfAbsenceLearnerToday(int courseId);

  @Select("select count(p.id) from participation p join learner_enrollment le on p.learner_enrollment_id = le.id and le.course_id = #{courseId} where p.participation_date = current_date() and status in ('IN_STUDY', 'LATE', 'ATTENDANCE')")
  int selectCountOfInStudyLearnerToday(int courseId);

  @Select("select count(ts.id) from test_submission ts join (select * from test where course_id = #{courseId} and current_timestamp() between start_date and end_date) t on ts.test_id = t.id where submission_status = 'NOT_STARTED'")
  int selectCountOfNotSubmitTestLearner(int courseId);

  @Select("select count(h.id) from homework h left join learner_enrollment le on h.course_id = le.course_id left join homework_submission hs on le.user_id = hs.learner_id and hs.homework_id = h.id where h.course_id = #{courseId} and current_timestamp() between h.start_date and h.end_date and hs.id is null and le.completion_status = 'IN_PROGRESS'")
  int setCountOfNotSubmitHomeworkLearner(int courseId);

  @Select("select count(hs.id) from homework_submission hs left join homework_eval he on hs.id = he.hs_id left join homework h on hs.homework_id = h.id where he.id is null and h.course_id = #{courseId} and current_date() between h.start_date and h.end_date")
  int selectCountOfNotEvalHomework(int courseId);

  @Select("select * from participation p join learner_enrollment le on p.learner_enrollment_id = le.id and le.course_id = #{courseId} where p.status = 'VACATION_PENDING'")
  int selectCountOfNotApproveVacation(int courseId);

  @Select("select id as test_id, title, total_score from test where course_id = #{courseId} and end_date < current_date()")
  List<CustomTestDTO> selectTestByCourseId(int courseId);

  @Select("select ts.id as test_submission_id, ts.learner_id, ts.score, u.fullname as learner_name from test_submission ts join user u on ts.learner_id = u.id where ts.test_id = #{testId}")
  List<CustomTestSubmissionDTO> selectTestSubmission(CustomTestDTO customTestDTO);
}
