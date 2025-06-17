package com.goott5.lms.learnermanagement.mapper;

import com.goott5.lms.coursemanagement.domain.PageListReqDTO;
import com.goott5.lms.learnermanagement.domain.*;

import java.util.List;

import com.goott5.lms.learnermanagement.domain.homework.HomeworkRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.test.TestRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
