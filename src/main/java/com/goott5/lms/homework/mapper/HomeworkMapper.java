package com.goott5.lms.homework.mapper;

import com.goott5.lms.homework.domain.*;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HomeworkMapper {


  // 공통으로 쓰임
  @Select("select login_id from user \n"
      + "where id = #{id}")
  String selectInstructorIdForHomework(int id);

  // 로그인한 아이디가 속한 과정 출력(학생,강사)
  //1. 학생
  @Select("select c.name from course c "
      + "inner join learner_enrollment le "
      + "on c.id = le.course_id "
      + "inner join user u "
      + "on le.user_id = u.id "
      + "where u.id = (select id from user where login_id = #{loginId}) "
      + "order by start_date desc")
  List<String> selectCourseMenuForLearner(String loginId);

  //2. 강사
  @Select("select c.name from course c "
      + "inner join staff_assignment sa "
      + "on c.id = sa.course_id "
      + "inner join user u "
      + "on sa.user_id = u.id "
      + "where u.id = (select id from user where login_id = #{loginId}) "
      + "order by start_date desc")
  List<String> selectCourseMenuForTeacher(String loginId);

  // 관리자용 select 박스 내용 출력(과정명)
  List<String> selectBoxCourseNameForAdmin(Boolean isInProgress);


  //강사용 homework조회
  List<HomeworkDTO> searchBySelectForTeacher(HomeworkRequestDTO homeworkRequestDTO);

  //total 게시물(페이징에 필요한) 수-강사
  int searchCountForTeacher(HomeworkRequestDTO homeworkRequestDTO);

  //학생용 homework 조회
  List<HomeworkDTO> searchBySelectForLearner(HomeworkRequestDTO homeworkRequestDTO);

  //total 게시물(페이징에 필요한) 수-교육생
  int searchCountForLearner(HomeworkRequestDTO homeworkRequestDTO);

  //관리자용 homework조회
  List<HomeworkDTO> searchBySelectForAdmin(HomeworkRequestDTO homeworkRequestDTO);

  //total 게시물(페이징에 필요한) 수-관리자
  int searchCountForAdmin(HomeworkRequestDTO homeworkRequestDTO);


  //위 homeworkDTO에 맞는 submission 조회
  @Select("select id, homework_id, title, content, read_count, learner_id, created_at, updated_at, deleted_at from homework_submission where homework_id = #{homeworkId} limit #{pagingRequest.skip},#{pagingRequest.pageSize}")
  List<HomeworkSubmissionDTO> selectSubmissionById(int homeworkId, PagingRequestDTO pagingRequest);

  //submission count
  @Select("select count(*) from homework_submission where homework_id = #{homeworkId}")
  int totalSubmission(int homeworkId);

  //--------- 상세 페이지------------------------------------------------------------------------

  // 과제 아이디-> 과제 상세 페이지 반환
  @Select(
      "select id,title,start_date,end_date,content,course_id,read_count,instructor_id , created_at, updated_at, deleted_at "
          +
          "from homework where id = #{id}")
  HomeworkDTO selectHomeworkDTOById(int id);


  //submission 상세 확인 시, 아이디 검사
  @Select("select login_id from user \n"
      + "where id = (select learner_id from homework_submission where id = #{id})")
  String selectUserIdForSubmission(int id);

  //submission 정보 조회(submission data)
  @Select("select id, homework_id, title, content, " +
      " read_count, learner_id, created_at, updated_at, deleted_at " +
      " from homework_submission " +
      " where id = #{submissionId}")
  HomeworkSubmissionDTO selectSubmissionBySubmissionId(int submissionId);

  //submissionId에 따른 eval 조회
  @Select("select id, hs_id, is_pass, content, read_count, instructor_id, created_at, updated_at, deleted_at from homework_eval where hs_id = #{hsId}")
  HomeworkEvalDTO selectEvalById(int hsId);

  //--------과제 등록------------------------------------------------------------------------

  // 강사의 과제 등록
  int insertHomework(HomeworkDTO homeworkDTO);

  //select한 nameforlt가 isInProgress인지 확인
  @Select("select is_in_progress from course where name = #{name}")
  Boolean selectIsInProgress(String name);

  //(sa에 등록된) select박스에서 선택한 과정과 로그인한 강사가 일치하지 않을 때 막기
//  @Select("select exists(select  sa.course_id\n"
//      + "from staff_assignment sa\n"
//      + "inner join course c\n"
//      + "on sa.course_id = c.id\n"
//      + "inner join user u\n"
//      + "on sa.user_id = u.id\n"
//      + "where c.name = #{name}\n"
//      + "and sa.user_id = #{userId}\n"
//      + "and u.type = #{type}"
//      + "and c.is_in_progress = 1)")
//  Boolean selectIsInProgressForSelectBox(String name, int userId, String type);

  //강사의 과제 등록을 위한 instructorId와 courseId select
  @Select("select  sa.course_id, sa.user_id\n"
      + "from staff_assignment sa\n"
      + "inner join course c\n"
      + "on sa.course_id = c.id\n"
      + "inner join user u\n"
      + "on sa.user_id = u.id\n"
      + "where c.name = #{name}\n"
      + "and sa.user_id = #{userId}\n"
      + "and u.type = #{type}"
      + "and c.is_in_progress = 1")
  List<Map<String, Integer>> selectForInsertId(String name, int userId, String type);

  //-------- 과제 수정 ---------------------------------------------------------------------

  //해당 과제의 작성자 id가 로그인한 강사 id와 일치하는지 확인.
  @Select("SELECT EXISTS (\n"
      + "  SELECT 1\n"
      + "  FROM homework \n"
      + "  WHERE instructor_id IN (\n"
      + "    SELECT id FROM user WHERE login_id = #{loginId}\n"
      + "  )\n"
      + "  AND id = #{homeworkId}\n"
      + ")")
  int selectIsInstructorId(String loginId, int homeworkId);

  // 해당 homeworkId 에 해당하는 과정 아이디 조회
//    @Select("select h.instructor_id, h.course_id\n" +
//        "from homework h\n" +
//        "inner join user u\n" +
//        "on h.instructor_id = u.id\n" +
//        "inner join course c\n" +
//        "on h.course_id = c.id\n" +
//        "where u.login_id = #{loginId}\n" +
//        "and h.id = #{homeworkId}")
//    List<Map<String, Integer>> selectForUpdateId(String loginId, int homeworkId);

  // 과제 업데이트(homeworkModifyDTO)
  @Update("update homework set title = #{title}, start_date = #{startDate}, end_date = #{endDate}, content = #{content} where id = #{id}")
  int updateHomework(HomeworkModifyDTO homeworkModifyDTO);

  //-------과제 delete-----------------
  @Delete("delete from homework where id = #{id}")
  int deleteHomeworkById(int id);

  //------------ 조회수--------------

  //조회수 업데이트
  @Update("update homework set read_count = read_count + 1 where id = #{tableId}")
  int updateReadCount(int tableId);

  //--------테스트용------------------------------------------------------------------------

  //homework_submission insert(일단 더미데이터용)
  @Insert("insert into homework_submission(homework_id,  title, content, learner_id) values(#{homeworkId}, #{title}, #{content}, #{learnerId})")
  int insertHomeworkSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);


}
