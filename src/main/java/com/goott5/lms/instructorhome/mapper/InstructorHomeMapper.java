package com.goott5.lms.instructorhome.mapper;

import com.goott5.lms.instructorhome.domain.CourseScheduleVO;
import com.goott5.lms.instructorhome.domain.CourseVO;
import com.goott5.lms.instructorhome.domain.CustomCourseForumVO;
import com.goott5.lms.instructorhome.domain.CustomCourseNoticeVO;
import com.goott5.lms.instructorhome.domain.CustomCourseQnAVO;
import com.goott5.lms.instructorhome.domain.CustomCourseVO;
import com.goott5.lms.instructorhome.domain.CustomInquiryVO;
import com.goott5.lms.instructorhome.domain.CustomTestDTO;
import com.goott5.lms.instructorhome.domain.CustomTestSubmissionDTO;
import com.goott5.lms.instructorhome.domain.EnrolledLearnerVO;
import com.goott5.lms.user.domain.UserVO;
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

  @Select("select count(ts.id) from test_submission ts join (select * from test where course_id = #{courseId} and current_timestamp() between start_date and end_date) t on ts.test_id = t.id join learner_enrollment le on ts.learner_id = le.user_id and le.course_id = #{courseId} where submission_status = 'NOT_STARTED' and le.completion_status != 'IN_PROGRESS'")
  int selectCountOfNotSubmitTestLearner(int courseId);

  @Select("select count(h.id) from homework h left join learner_enrollment le on h.course_id = le.course_id left join homework_submission hs on le.user_id = hs.learner_id and hs.homework_id = h.id where h.course_id = #{courseId} and current_timestamp() between h.start_date and h.end_date and hs.id is null and le.completion_status = 'IN_PROGRESS'")
  int setCountOfNotSubmitHomeworkLearner(int courseId);

  @Select("select count(hs.id) from homework_submission hs left join homework_eval he on hs.id = he.hs_id left join homework h on hs.homework_id = h.id where he.id is null and h.course_id = #{courseId} and current_date() between h.start_date and h.end_date")
  int selectCountOfNotEvalHomework(int courseId);

  @Select("select count(*) from participation p join learner_enrollment le on p.learner_enrollment_id = le.id and le.course_id = #{courseId} where p.status = 'VACATION_PENDING'")
  int selectCountOfNotApproveVacation(int courseId);

  @Select("select id as test_id, title, total_score from test where course_id = #{courseId} and end_date < current_timestamp() order by end_date")
  List<CustomTestDTO> selectTestByCourseId(int courseId);

  @Select("select ts.id as test_submission_id, ts.learner_id, ts.score, u.fullname as learner_name from test_submission ts join user u on ts.learner_id = u.id join test t on t.id = ts.test_id join learner_enrollment le on t.course_id = le.course_id and le.user_id = u.id where ts.test_id = #{testId} order by ts.learner_id")
  List<CustomTestSubmissionDTO> selectTestSubmission(CustomTestDTO customTestDTO);

  @Select("select cq.id, cq.title, cq.created_at, u.id as writer_id, u.fullname as writer_name from course_qna cq join user u on cq.writer_id = u.id where course_id = #{courseId} and is_answer = 0 order by created_at desc limit 3")
  List<CustomCourseQnAVO> selectCourseQnA(int courseId);

  @Select("select id, title, is_answered, is_answered_checked, created_at from community_inquiry where writer = #{id} order by created_at desc limit 3")
  List<CustomInquiryVO> selectInquiry(UserVO loginUser);

  @Select("select cn.id, cn.title, u.id as writer_id, u.fullname as writer_name, cn.is_fixed, cn.created_at from course_notice cn join user u on cn.writer_id = u.id where cn.course_id = #{courseId} and cn.is_fixed = 1 union (select cn.id, cn.title, u.id as writer_id, u.fullname as writer_name, cn.is_fixed, cn.created_at from course_notice cn join user u on cn.writer_id = u.id where cn.course_id = #{courseId} and cn.is_fixed = 0 order by cn.created_at desc limit 2) order by is_fixed desc, created_at desc")
  List<CustomCourseNoticeVO> selectCourseNotice(int courseId);

  @Select("select cf.id, cf.title, u.id as writer_id, u.fullname as writer_name, cf.created_at , cf.forum_like, count(cfc.id) as comment_count from course_forum cf left join user u on cf.writer_id = u.id left join course_forum_comment cfc on cf.id = cfc.course_forum_id where cf.is_hotpost = 1 and cf.course_id = #{courseId} group by cf.id order by created_at desc")
  List<CustomCourseForumVO> selectCourseForum(int courseId);

  @Select("select c.id, c.name from course c join staff_assignment sa on c.id = sa.course_id and sa.user_id = #{id} order by c.end_date desc")
  List<CourseVO> selectCourseLists(UserVO loginUser);

  @Select("select count(id) from training_log where course_id = #{courseId} and training_date = current_date()")
  int selectTrainingLogCount(int courseId);

  List<CourseScheduleVO> selectCourseSchedules(@Param("today") LocalDate today , @Param("courseId") int courseId);

  @Select("select u.id, u.fullname from user u join learner_enrollment le on u.id = le.user_id and le.course_id = #{courseId} order by u.id")
  List<EnrolledLearnerVO> selectEnrolledLearners(int courseId);

  @Select("select c.name from classroom c join classroom_allocation ca on c.id = ca.classroom_id and ca.course_id = #{courseId}")
  String selectClassroom(int courseId);
}
