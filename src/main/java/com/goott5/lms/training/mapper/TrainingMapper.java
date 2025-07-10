package com.goott5.lms.training.mapper;

import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectTrainingForListDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDetailDTO;
import com.goott5.lms.training.domain.registerdto.SelectCourseDTO;
import com.goott5.lms.training.domain.registerdto.SelectSchSubDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TrainingMapper {
  //================== 공통 ============================

//  //courseId 출력
//  @Select("select id from course where name = #{name}")
//  int selectCourseIdByName(String name);

  //현재 강사가 진행 중인 과정 아이디, 과정명 출력
  @Select("select c.id,c.name from course c\n"
      + "inner join staff_assignment sa\n"
      + "on c.id = sa.course_id\n"
      + "where c.is_in_progress = 1 \n"
      + "and sa.user_id = #{userId}")
  SelectCourseDTO selectCourse(int userId);

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

  //강사 아이디로 강사명 찾기
  @Select("select fullname from user where id = #{instructorId}")
  String selectInstructorNameById(int instructorId);

  // 과정 아이디로 staff-Assignment에서 관리자 id 출력
  @Select("select user_id from staff_assignment where course_id = #{courseId}")
  List<Integer> selectStaffIdByCourseId(int courseId);

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
  List<SelectTrainingForListDTO> selectTrainingLogForAdmin(String courseName);

  //강사용 select training_log list
  List<SelectTrainingForListDTO> selectTrainingLogForTeacher(int instructorId, String courseName);

  //======== 훈련일지 상세 ==========================================

  //훈련일지 상세 select
  @Select("select id,course_id,training_date,instructor_id from training_log where id = #{id}")
  SelectTrainingDTO selectTrainingLog(int id);

  //훈련일지 상세 detail select
  @Select("select id, training_id, period, plan, actual from training_detail where training_id = #{training_id}")
  List<SelectTrainingDetailDTO> selectTrainingDetail(int trainingId);

  //course_schedule의 id로 훈련과목 select
  @Select("select sb.name\n"
      + "from course_subject sb\n"
      + "inner join course_schedule sc\n"
      + "on sb.id = sc.subject_id\n"
      + "where sc.id = #{plan}")
  String selectSubjectName(int plan);

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

  //=====교시/훈련과목===========
  @Select("select sch.id,sch.subject_id,sch.period,sub.name\n"
      + "from course_schedule sch\n"
      + "inner join course_subject sub\n"
      + "on sch.subject_id = sub.id\n"
      + "inner join staff_assignment sa\n"
      + "on sa.course_id = sch.course_id\n"
      + "where sch.class_date = #{trainingDate}\n"
      + "and sa.user_id = #{userId}")
  List<SelectSchSubDTO> selectSchSub(Date trainingDate, int userId);

  //================== 훈련일지 등록 ========================
  @Insert("insert into training_log (course_id,training_date,instructor_id) values (#{courseId},#{trainingDate},#{instructorId})")
  int insertTrainingLog(InsertTrainingDTO insertTrainingDTO);

  @Insert("insert into training_detail (training_id,period,plan,actual) values (#{trainingId}, #{period}, #{plan}, #{actual})")
  int insertTrainingDetail(InsertTrainingDetailDTO insertTrainingDetailDTO);

  // 휴강일 확인
  @Select("select exists\n"
      + "(select 1 from cancel_date where cancel_date = #{trainingDate})")
  boolean isHoliday(String trainingDate);

  // 재등록 막기(현재 날짜로 등록할 경우 막기)
  @Select("select exists(select 1 from training_log where training_date = #{trainingDate} and instructor_id = #{instructorId} and course_id = #{courseId})")
  boolean isReRegister(String trainingDate, int instructorId, int courseId);

  // 등록하려는 날짜가 해당 과정의 start_date와 end_date 사이에 있는 지 확인
  @Select("select exists (select 1 from course where #{trainingDate} between start_date and end_date \n"
      + "and id = #{courseId})")
  boolean isRegisterDate(String trainingDate, int courseId);

  //=================== 훈련일지 수정 ===========================
  @Update("update training_detail set actual = #{actual}, updated_at = now() where id = #{trainingId}")
  int updateTrainingDetail(int trainingId, String actual);

  @Update("update training_log set updated_at = now() where id = #{trainingId}")
  int updateTrainingLog(int trainingId);

  // 강사 권한 확인
  @Select("select exists(select 1 from training_log where id = #{id} and instructor_id = #{instructorId})")
  boolean isMyTrainingLog(int id, int instructorId);

  // 관리자 권한 확인(해당 과정의 관리자인지)
  @Select("select exists(select 1 from staff_assignment where course_id = #{courseId} and user_id = #{userId})")
  boolean isAdmin(int courseId, int userId);

  // ======================= 훈련일지 삭제 ===============================
  @Delete("delete from training_detail where training_id = #{trainingId}")
  int deleteTrainingDetail(int trainingId); //1보다 클 수 o

  @Delete("delete from training_log where id = #{trainingId}")
  int deleteTrainingLog(int trainingId);

  //====================== 훈련일지 서명 ==============================

  // ===== 슈퍼 매니저 ==========
  @Select("select exists (\n"
      + "select 1 from staff_detail sd\n"
      + "inner join user u\n"
      + "on sd.user_id = u.id\n"
      + "where sd.position = 'GENERAL_MANAGER' and sd.user_id = #{userId})")
  boolean isSuperAdmin(int userId);

}
