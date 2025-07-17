package com.goott5.lms.homework.mapper;

import com.goott5.lms.homework.domain.*;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

@Mapper
public interface HomeworkMapper {


//  // 공통으로 쓰임
//  @Select("select login_id from user \n"
//      + "where id = #{id}")
//  String selectInstructorIdForHomework(int id);

  // 공통으로 쓰임 (loginId => fullname으로 변경)
  @Select("select fullname from user \n"
      + "where id = #{id}")
  String selectInstructorIdForHomework(int id);

  // 과정 아이디로 과정명 가져오기
  @Select("select name from course where id = #{id}")
  String courseName(int id);

  //homeworkId로 과정 아이디 가져오기
  @Select("select course_id from homework where id = #{id}")
  int courseId(int id);

  // 해당 과정 아이디의 instructorId 가져오기(homework 에서)
  @Select("select instructor_id from homework where course_id = #{courseId} limit 1")
  int instructorIdByCourseId(int id);

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
//  @Select("select id, homework_id, title, content, read_count, learner_id, created_at, updated_at, deleted_at from homework_submission where homework_id = #{homeworkId} limit #{pagingRequest.skip},#{pagingRequest.pageSize}")
//  List<HomeworkSubmissionDTO> selectSubmissionById(int homeworkId, PagingRequestDTO pagingRequest);

  // submission에 교육생 이름 넣어서 다시 조회(위의 쿼리문 대신)
//  @Select("select hs.id, hs.homework_id, hs.title, hs.content, hs.read_count, hs.learner_id, hs.created_at, hs.updated_at, hs.deleted_at, u.fullname\n"
//      + "from homework_submission hs\n"
//      + "inner join user u\n"
//      + "on hs.learner_id = u.id\n"
//      + "where  hs.homework_id = #{homeworkId} limit #{pagingRequest.skip},#{pagingRequest.pageSize}")
//  List<HomeworkSubmissionForListDTO> selectSubmissionById(int homeworkId, PagingRequestDTO pagingRequest);

  // submission에 교육생 이름 넣어서 다시 조회(위의 쿼리문 대신)
  @Select("select hs.id, hs.homework_id, hs.title, hs.content, hs.read_count, hs.learner_id, hs.created_at, hs.updated_at, hs.deleted_at, u.fullname, he.hs_id\n"
      + "from homework_submission hs \n"
      + "inner join user u \n"
      + "on hs.learner_id = u.id\n"
      + "left outer join homework_eval he\n"
      + "on hs.id = he.hs_id\n"
      + "where  hs.homework_id = #{homeworkId} limit #{pagingRequest.skip},#{pagingRequest.pageSize}")
  List<HomeworkSubmissionForListDTO> selectSubmissionById(int homeworkId, PagingRequestDTO pagingRequest);


  //submission count
  @Select("select count(*) from homework_submission where homework_id = #{homeworkId}")
  int totalSubmission(int homeworkId);

  //submission의 homeworkId에 따른 homework명 반환
  @Select("select title from homework where id = #{id}")
  String selectTitle(int id);

  //(기능 추가) submission 테이블에 homework_id와 loginUser가 있는 submissionId 반환
  // (테스트 도중 한 homeworkId에 중복되는 learnerId의 과제를 받아놓았기에, 리스트로 받아야함.)
  @Select("select id from homework_submission where homework_id = #{homeworkId} and learner_id = #{learnerId}")
  List<Integer> selectSubmissionIdForLearner(int homeworkId, int learnerId);


  //--------- 상세 페이지------------------------------------------------------------------------

  // 과제 아이디-> 과제 상세 페이지 반환
  @Select(
      "select id,title,start_date,end_date,content,course_id,read_count,instructor_id , created_at, updated_at, deleted_at "
          +
          "from homework where id = #{id}")
  HomeworkDTO selectHomeworkDTOById(int id);


  //--------과제 등록------------------------------------------------------------------------

  // 강사의 과제 등록
  int insertHomework(HomeworkDTO homeworkDTO);

  //select한 nameforlt가 isInProgress인지 확인
  @Select("select is_in_progress from course where name = #{name}")
  Boolean selectIsInProgress(String name);

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

  //(기능 추가)해당 과제의 작성자 id가 로그인한 강사 id와 일치하는지 확인.
  @Select("SELECT EXISTS (\n"
      + "  SELECT 1\n"
      + "  FROM homework \n"
      + "  WHERE instructor_id IN (\n"
      + "    SELECT id FROM user WHERE id = #{userId}\n"
      + "  )\n"
      + "  AND id = #{homeworkId}\n"
      + ")")
  int selectIsInstructorIdByPk(int userId, int homeworkId);



  // 과제 업데이트(homeworkModifyDTO)
  @Update("update homework set title = #{title}, start_date = #{startDate}, end_date = #{endDate}, content = #{content}, updated_at = #{updatedAt} where id = #{id}")
  int updateHomework(HomeworkModifyDTO homeworkModifyDTO);

  //-------과제 delete-----------------
  @Delete("delete from homework where id = #{id}")
  int deleteHomeworkById(int id);

  @Select("select exists (select id from homework_submission where homework_id = #{homeworkId});")
  boolean selectHomeworkSubmissionIdByHomework(int homeworkId);


  //------------ 조회수--------------

  //조회수 업데이트(homework)
  @Update("update homework set read_count = read_count + 1 where id = #{tableId}")
  int updateReadCount(int tableId);

  //조회수 업데이트(homework_submission)
  @Update("update homework_submission  set read_count = read_count + 1 where id = #{tableId}")
  int updateReadCountForSubmission(int tableId);

  //조회수 업데이트(homework_eval)
  @Update("update homework_eval  set read_count = read_count + 1 where id = #{tableId}")
  int updateReadCountForEval(int tableId);

  //-------submission용----------------------------------------------------

  //submission 상세 확인 시, 아이디 검사
  @Select("select login_id from user \n"
      + "where id = (select learner_id from homework_submission where id = #{id})")
  String selectUserIdForSubmission(int id);

  @Select("select id from user \n"
      + "where id = (select learner_id from homework_submission where id = #{submissionId})")
  Integer selectUserPkForSubmission(int submissionId);

  //submission 정보 조회(submission data)
  @Select("select id, homework_id, title, content, " +
      " read_count, learner_id, created_at, updated_at, deleted_at " +
      " from homework_submission " +
      " where id = #{submissionId}")
  HomeworkSubmissionDTO selectSubmissionBySubmissionId(int submissionId);

  //submissionId에 따른 eval 조회
  @Select("select id, hs_id, is_pass, content, read_count, instructor_id, created_at, updated_at, deleted_at from homework_eval where hs_id = #{hsId}")
  HomeworkEvalDTO selectEvalById(int hsId);

  //submissionId로 해당 homeworkDTO 조회
  @Select("select id, title, start_date, end_date, content, course_id, read_count, instructor_id , created_at, updated_at, deleted_at "
      + "from homework where id = (select homework_id from homework_submission where id = #{submissionId})")
  HomeworkDTO selectHomeworkDTOBySubmissionId(int submissionId);

  // (기능 추가) 해당 homework_submission의 homeworkId가 속한 course_id가 현재 진행 중인지 확인
  @Select("select is_in_progress from course where id = (select h.course_id \n"
      + "from homework h \n"
      + "where h.id = #{homeworkId})")
  Boolean isInProgressByHomeworkId(int homeworkId);

  //(유효성 추가) 제출하려하는 homework의 제출기한이 아직 시작되지 않았을 때 등록 막기 (boolean)
  @Select("select start_date >= current_timestamp()\n"
      + "from homework\n"
      + "where id = #{homeworkId}")
  Boolean isNotStart(int homeworkId);

  //--------테스트용 + insertSubmission------------------------------------------------------------------------

  //homework_submission insert(일단 더미데이터용 => insertSubmission)
  @Insert("insert into homework_submission(homework_id,  title, content, learner_id) values(#{homeworkId}, #{title}, #{content}, #{learnerId})")
  int insertHomeworkSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);


//  //insert homeworkSubmission
//  @Insert("insert into homework_submission (title,  content, course_id, learner_id )\n"
//      + "    values (#{title},  #{content}, #{courseId}, #{learnerId})")
//  HomeworkSubmissionDTO insertSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);

  //해당 과제의 과정에 속한 학생인지 확인
  @Select("select exists (select user_id from learner_enrollment where user_id = #{userId} and course_id = (select course_id from homework where id = #{homeworkId}))")
  int isLearnerInCourse(int userId, int homeworkId);

  // 해당 학생이 해당 과제에 대한 제출기록이 1개 이상 존재하는지 확인(맞으면 막기)
  @Select("select exists (select id from homework_submission where learner_id = #{learnerId} and homework_id = #{homeworkId})")
  int existSubmission(int learnerId, int homeworkId);

  //submission 업데이트(수정)
  @Update("update homework_submission set title = #{title}, content = #{content}, updated_at = #{updatedAt} where id = #{id}")
  int updateSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);

  //submission delete
  @Delete("delete from homework_submission where id = #{id}")
  int deleteSubmissionById(int id);

  @Insert("insert into homework_eval (hs_id, is_pass, content, instructor_id) values (#{hsId}, #{isPass}, #{content}, #{instructorId})")
  int insertEval(HomeworkEvalDTO homeworkEvalDTO);

  // updateEval
  @Update("update homework_eval set is_pass = #{isPass}, content = #{content}, updated_at = #{updatedAt} where id = #{id}")
  int updateEval(HomeworkEvalModifyDTO homeworkEvalModifyDTO);

  //deleteEval
  @Delete("delete from homework_eval where id = #{id}")
  int deleteEvalById(int id);


}
