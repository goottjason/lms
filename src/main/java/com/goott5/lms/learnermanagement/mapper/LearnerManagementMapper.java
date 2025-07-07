package com.goott5.lms.learnermanagement.mapper;

import com.goott5.lms.coursemanagement.domain.PageListReqDTO;
import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.learnermanagement.domain.*;

import com.goott5.lms.learnermanagement.domain.dto.CompletionStatusUpdateRequest;
import com.goott5.lms.learnermanagement.domain.dto.LearnerResponse;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.PartModifyRequest;
import com.goott5.lms.learnermanagement.domain.integrated.HomeworkOverviewResp;
import com.goott5.lms.learnermanagement.domain.table.EmploymentSupport;
import com.goott5.lms.learnermanagement.domain.table.HomeworkWithSubEval;
import com.goott5.lms.learnermanagement.domain.table.ParticipationWithReason;
import com.goott5.lms.learnermanagement.domain.table.TestWithSub;
import com.goott5.lms.learnermanagement.domain.table.User;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import java.util.List;

import com.goott5.lms.learnermanagement.domain.homework.HomeworkRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.test.TestRespDTO;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LearnerManagementMapper {

  List<UserRespDTO> selectLearnersByCondition(
          @Param("pageListReqDTO") PageListReqDTO pageListReqDTO,
          @Param("userId") Integer userId,
          @Param("courseId") Integer courseId,
          @Param("isInProgress") Boolean isInProgress);

  // LearnerManagementMapper.xml에 추가
  List<UserRespDTO> selectLearnersAll(
          @Param("pageUserReqDTO") PageUserReqDTO pageUserReqDTO,
          @Param("loginUserId") Integer loginUserId,
          @Param("loginUserType") String loginUserType,
          @Param("courseId") Integer courseId,
          @Param("isInProgress") Boolean isInProgress
  );

  List<LearnerRespDTO> selectLearnerListOrDetail(
    @Param("pageLearnerReqDTO") PageLearnerReqDTO<LearnerReqDTO> pageLearnerReqDTO,
    @Param("loginUserId") Integer loginUserId,
    @Param("loginUserType") String loginUserType,
    @Param("leId") Integer leId,
    @Param("isInProgress") Boolean isInProgress,
    @Param("courseId") Integer courseId
  );

  List<TestRespDTO> selectTestsByIds(
    @Param("loginUserId") Integer loginUserId,
    @Param("loginUserType") String loginUserType,
    @Param("enrolledCourseId") Integer enrolledCourseId,
    @Param("learnerId") Integer learnerId);

  List<HomeworkRespDTO> selectHomeworksByIds(
    @Param("loginUserId") Integer loginUserId,
    @Param("loginUserType") String loginUserType,
    @Param("enrolledCourseId") Integer enrolledCourseId,
    @Param("learnerId") Integer learnerId);


  Integer selecttotalRecordsFromParticipation(Integer leId);

  List<ParticipationRespDTO> selectParticipationsByIds(
    @Param("loginUserId") Integer loginUserId,
    @Param("loginUserType") String loginUserType,
    @Param("leId") Integer leId);

  List<ParticipationRespDTO> selectParticipationsByIdsWithPaging(
    @Param("pageParticipationReqDTO") PageParticipationReqDTO<ParticipationReqDTO> pageParticipationReqDTO,
    @Param("loginUserId") Integer loginUserId,
    @Param("loginUserType") String loginUserType,
    @Param("leId") Integer leId);

  Boolean updateEmploymentSupport(
    @Param("loginId") Integer loginId,
    @Param("loginUserType") String loginUserType,
    @Param("leId") Integer leId,
    @Param("employmentSupportReqDTO") EmploymentSupportReqDTO employmentSupportReqDTO);

  List<LearnerRespDTO> selectLearnersAllorOne(
      @Param("base") BaseReqDTO base,
      @Param("page") PageLearnerReqDTO<LearnerReqDTO> pageLernerReqDTO);

  List<LearnerResponse> selectLearnersByAuth(
      @Param("base") BaseReqDTO base,
      @Param("page") PageLearnerRequest page);





  User selectUserById(Integer userId);

  EmploymentSupport selectEmploymentSupportByLeId(Integer leId);

  List<ParticipationWithReason> selectParticipationByLeId(Integer leId);

  List<HomeworkWithSubEval> selectHomeworkByCourseIdAndUserId(
      @Param("leCourseId") Integer leCourseId, @Param("leUserId") Integer leUserId);

  List<TestWithSub> selectTestByCourseIdAndUserId(
      @Param("leCourseId") Integer leCourseId, @Param("leUserId") Integer leUserId);

  CourseWithAssignedInfo selectCourseByCourseId(Integer leCourseId);

  String selectLoginUserPositionByUserId(Integer loginUserId);

  ParticipationWithReason selectPartInfoByPid(Integer pid);

  Boolean updateCompletionStatus(CompletionStatusUpdateRequest request);

  List<LearnerResponse> selectLearnersByAuthByCoIds(
      @Param("base") BaseReqDTO baseReqDTO,
      @Param("coIds") List<Integer> coIds);

  List<LearnerResponse> selectLearnersOnlyPartByAuth(
      @Param("base") BaseReqDTO baseReqDTO,
      @Param("page") PageLearnerRequest pageLearnerRequest);

  @Select("SELECT id FROM participation_reason WHERE participation_id = #{partId}")
  Integer selectIsParticipationReasonRow(Integer partId);
  @Insert("INSERT INTO participation_reason (participation_id, explanation) VALUES (#{partId}, #{partExplanation});")
  void insertParticipationReason(PartModifyRequest request);

  @Update("UPDATE participation SET check_in = #{partCheckIn}, check_out=#{partCheckOut}, status = #{partStatus}, training_time = #{partTrainingTime} WHERE id = #{partId}")
  void updateParticipationByRequest(PartModifyRequest request);

  @Update("UPDATE participation_reason SET explanation = #{partExplanation} WHERE participation_id = #{partId}")
  void updateParticipationReasonByRequest(PartModifyRequest request);

  @Delete("DELETE FROM participation_reason WHERE participation_id = #{partId}")
  void deleteParticipationReasonByRequest(PartModifyRequest request);
}
