package com.goott5.lms.coursemanagement.mapper;

import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.CourseSubjectRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageListReqDTO;
import com.goott5.lms.learnermanagement.domain.PageUserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserReqDTO;
import com.goott5.lms.learnermanagement.domain.UserRespDTO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseManagementMapper {

  /**
   * 전체 과정 리스트 조회 API
   *
   * @param pageCourseReqDTO
   * @param loginUserId
   * @param loginUserType
   * @param isInProgress
   * @return
   */
  List<CourseRespDTO> selectCoursesAllorOne(
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO,
      Integer loginUserId,
      String loginUserType,
      Boolean isInProgress,
      Integer courseId);

  /**
   * 과정에 배정된 강사 조회
   *
   * @param courseRespDTO
   * @return
   */
  @Select({
      "SELECT fullname FROM user",
      "WHERE id in (",
      "  SELECT user_id FROM staff_assignment",
      "    WHERE course_id = #{courseRespDTO.id}) AND type = 'INSTRUCTOR'"
  })
  String selectInstructorFullname(@Param("courseRespDTO") CourseRespDTO courseRespDTO);

  /**
   * 과정에 배정된 강의실 조회
   *
   * @param courseRespDTO
   * @return
   */
  @Select({
      "SELECT name FROM classroom",
      "WHERE id in (",
      "  SELECT classroom_id FROM classroom_allocation",
      "    WHERE course_id = #{courseRespDTO.id})"
  })
  String selectClassroomName(@Param("courseRespDTO") CourseRespDTO courseRespDTO);


  CourseRespDTO selectCourse(Integer loginUserId, String loginUserType, Integer courseId);

  /**
   * 과정에 배정된 교과목 조회
   *
   * @param courseId
   * @return
   */
  @Select({
      "SELECT * FROM course_subject",
      "WHERE course_id = #{courseId} ORDER BY subject_order ASC"})
  List<CourseSubjectRespDTO> selectCourseSubjectById(Integer courseId);

  /**
   * 교육생 배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param courseId
   * @return
   */
  List<UserRespDTO> selectEnrolledLearnersByCourseId(
      @Param("pageUserReqDTO") PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @Param("courseId") Integer courseId
  );

  /**
   * 교육생 미배정 현황 조회 API
   *
   * @param pageUserReqDTO
   * @param includeAll
   * @return
   */
  List<UserRespDTO> selectNotEnrolledLearnersAll(
      @Param("pageUserReqDTO") PageUserReqDTO<UserReqDTO> pageUserReqDTO,
      @Param("includeAll") Boolean includeAll
  );

  /**
   * 교육생 배정 '추가' API
   *
   * @param userId
   * @param courseId
   * @return
   */
  @Insert("INSERT INTO learner_enrollment (user_id, course_id) VALUES (#{userId}, #{courseId})")
  int insertLearnerToCourse(
      @Param("userId") Integer userId,
      @Param("courseId") Integer courseId
  );

  /**
   * 교육생 배정 '삭제' API
   *
   * @param userId
   * @param courseId
   * @return
   */
  @Delete("DELETE FROM learner_enrollment WHERE user_id = #{userId} AND course_id = #{courseId}")
  int deleteLearnerFromCourse(
      @Param("userId") Integer userId,
      @Param("courseId") Integer courseId
  );

  /**
   * 해당하는 과정에 배정중인 교육생의 수 조회
   *
   * @param courseId
   * @return
   */
  @Select({
      "SELECT COUNT(*) FROM learner_enrollment WHERE course_id = #{courseId}"
  })
  Integer selectErolledLernerCount(Integer courseId);


  int deleteCourse(
      @Param("loginUserId") Integer loginUserId,
      @Param("loginUserType") String loginUserType,
      @Param("courseId") Integer courseId
  );
  @Select({
      "SELECT id FROM learner_enrollment WHERE user_id = #{learnerId} AND course_id = #{courseId}"
  })
  Integer selectLearnerEnrollmentByIds(
      @Param("learnerId") Integer learnerId,
      @Param("courseId") Integer courseId);

  @Insert({
      "INSERT INTO employment_support (learner_enrollment_id) VALUES (#{leId})"
  })
  int insertEmploymentSupport(@Param("leId") Integer leId);

}
