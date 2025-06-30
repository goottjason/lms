package com.goott5.lms.training.mapper;

import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrainingMapper {
  //================== 공통 ============================

  //과정아이디로 과정명 출력
  @Select("select name from course where id = #{courseId}")
  String selectCourseNameById(int courseId);

  // 해당 과정명이 종료 중인 과정명인지 판단(공통)
  @Select("select is_in_progress from course\n"
      + "where name = #{name}")
  boolean isInProgressCourse(String courseName);

  //해당 과정의 총 수강생 수
  @Select("select number_of_learner from course where id = #{courseId}")
  int countOfLearner(int courseId);



  // =============== select list ===========

  // 강사용 selectBox list
  @Select("select c.name from course c "
      + "inner join staff_assignment sa "
      + "on c.id = sa.course_id "
      + "inner join user u "
      + "on sa.user_id = u.id "
      + "where u.id = #{userId} "
      + "order by start_date desc")
  List<String> selectCourseMenuForTeacher(int userId);

  // 관리자용 select 박스 (과정명) list
  List<String> selectBoxCourseNameForAdmin(Boolean isInProgress);

  // 관리자용 select training_log list
  List<SelectTrainingDTO> selectTrainingLogForAdmin(String courseName);

  //강사용 select training_log list
  List<SelectTrainingDTO> selectTrainingLogForTeacher(int instructorId, String courseName);

  //======== 훈련일지 상세 ==========================================

  //훈련일지 상세 select
  @Select("select id,course_id,training_date,instructor_id from training_log where id = #{id}")
  SelectTrainingDTO selectTrainingLog(int id);

  //훈련일지 상세 detail select
  @Select("select id, training_id, period, plan, actual from training_detail where training_id = #{training_id}")
  List<SelectTrainingDetailDTO> selectTrainingDetail(int trainingId);

  //============== 출석과 관계된 쿼리문===============================

  // 출결 상태에 속하는 학생 수
  @Select("select count(p.id)\n"
      + "from participation p\n"
      + "inner join learner_enrollment le\n"
      + "on p.learner_enrollment_id = le.id\n"
      + "inner join course c\n"
      + "on le.course_id = c.id\n"
      + "where status = #{status} AND participation_date = #{trainingDate}\n"
      + "and c.id = #{courseId}")
  int countParticipation(RequestParticipationDTO request);

  //출결 상태에 속하는 학생 이름
  @Select("select u.fullname\n"
      + "from user u\n"
      + "inner join learner_enrollment le\n"
      + "on u.id = le.user_id\n"
      + "inner join participation p\n"
      + "on p.learner_enrollment_id = le.id\n"
      + "inner join course c\n"
      + "on le.course_id = c.id\n"
      + "where p.status = #{status} AND p.participation_date = #{trainingDate}\n"
      + "and c.id = #{courseId}")
  List<String> listParticipationLearner(RequestParticipationDTO request);

}
