package com.goott5.lms.learnermanagement.service;

import com.goott5.lms.coursemanagement.domain.table.CourseWithAssignedInfo;
import com.goott5.lms.coursemanagement.mapper.CourseManagementMapper;
import com.goott5.lms.learnermanagement.domain.*;
import com.goott5.lms.learnermanagement.domain.dto.LearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.LearnerResponse;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerRequest;
import com.goott5.lms.learnermanagement.domain.dto.PageLearnerResponse;
import com.goott5.lms.learnermanagement.domain.homework.HomeworkRespDTO;
import com.goott5.lms.learnermanagement.domain.integrated.HomeworkOverviewResp;
import com.goott5.lms.learnermanagement.domain.integrated.LearnerOverviewResp;
import com.goott5.lms.learnermanagement.domain.integrated.ParticipationOverviewResp;
import com.goott5.lms.learnermanagement.domain.integrated.TestOverviewResp;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.table.EmploymentSupport;
import com.goott5.lms.learnermanagement.domain.table.HomeworkWithSubEval;
import com.goott5.lms.learnermanagement.domain.table.ParticipationWithReason;
import com.goott5.lms.learnermanagement.domain.table.TestWithSub;
import com.goott5.lms.learnermanagement.domain.table.User;
import com.goott5.lms.learnermanagement.domain.test.TestRespDTO;
import com.goott5.lms.learnermanagement.mapper.LearnerManagementMapper;

import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearnerManagementServiceImpl implements LearnerManagementService {

  private final LearnerManagementMapper learnerManagementMapper;
  private final CourseManagementMapper courseManagementMapper;

  @Override
  public PageLearnerRespDTO<LearnerRespDTO> findLearnersAll(
    PageLearnerReqDTO<LearnerReqDTO> pageLearnerReqDTO,
    Integer loginUserId,
    String loginUserType,
    Integer leId,
    Boolean isInProgress,
    Integer courseId) {

    Integer originalPageNo = pageLearnerReqDTO.getPageNo();
    Integer originalPageSize = pageLearnerReqDTO.getPageSize();

    if (originalPageNo != null && originalPageSize != null) {
      pageLearnerReqDTO.setPageNo(null);
      pageLearnerReqDTO.setPageSize(null);
    }
    List<LearnerRespDTO> allLearners =
      learnerManagementMapper.selectLearnerListOrDetail(
        pageLearnerReqDTO,
        loginUserId,
        loginUserType,
        leId,
        isInProgress,
        courseId
      );
    int totalRecords = allLearners.size();

    // 페이지 정보가 있는 경우에만, 페이징 하여 리스트 조회 (없으면 상단에서 전체 리스트 조회)
    if (originalPageNo != null && originalPageSize != null) {
      pageLearnerReqDTO.setPageNo(originalPageNo);
      pageLearnerReqDTO.setPageSize(originalPageSize);
      // 페이징된 데이터만 조회
      allLearners =
        learnerManagementMapper.selectLearnerListOrDetail(
          pageLearnerReqDTO,
          loginUserId,
          loginUserType,
          leId,
          isInProgress,
          courseId
        );
    }


    if (!allLearners.isEmpty()) {

      for (LearnerRespDTO learner : allLearners) {
        log.info("★★★Learner: " + learner);
        // test 테이블에서 id만 가져오고, 그 id로 LEFT JOIN submission 해서
        // leId로 시험, 과제 조회 가능
        Integer learnerId = learner.getUserId();
        Integer enrolledCourseId = learner.getCourseId();
        log.info("enrolledCourseId: " + enrolledCourseId);
        log.info("learnerId: " + learnerId);

        List<TestRespDTO> testRespDTOS = learnerManagementMapper.selectTestsByIds(
          loginUserId,
          loginUserType,
          enrolledCourseId,
          learnerId
        );
        learner.setTestRespDTOS(testRespDTOS);

        List<HomeworkRespDTO> homeworkRespDTOS = learnerManagementMapper.selectHomeworksByIds(
          loginUserId,
          loginUserType,
          enrolledCourseId,
          learnerId
        );
        learner.setHomeworkRespDTOS(homeworkRespDTOS);


        Integer totalRecordsFromParticipation = learnerManagementMapper.selecttotalRecordsFromParticipation(learner.getLeId());

        List<ParticipationRespDTO> participationRespDTOS = learnerManagementMapper.selectParticipationsByIds(
          loginUserId,
          loginUserType,
          learner.getLeId()
        );
        HashMap<String, Integer> participationCountMap = new HashMap<String, Integer>();
        for (ParticipationRespDTO participationRespDTO : participationRespDTOS) {
          String status = participationRespDTO.getPStatus();
          participationCountMap.put(status, participationCountMap.getOrDefault(status, 0) + 1);
        }

        log.info("participationCountMap: " + participationCountMap);

        int attendance = participationCountMap.getOrDefault("ATTENDANCE", 0);
        int absence = participationCountMap.getOrDefault("ABSENCE", 0);
        int vacation = participationCountMap.getOrDefault("VACATION", 0);
        int late = participationCountMap.getOrDefault("LATE", 0);
        int leaveEarly = participationCountMap.getOrDefault("LEAVE_EARLY", 0);
        double x = (attendance * 1.0) + (vacation * 1.0) + (late * 0.5) + (leaveEarly * 0.5);
        int y = attendance + absence + vacation + late + leaveEarly;
        Double attendanceRate = 0.0;
        if (y > 0) {
          attendanceRate = Math.round((x/y) * 100 * 100.0) / 100.0;
        }

        PageParticipationRespDTO<ParticipationRespDTO> pageParticipationRespDTO
          = PageParticipationRespDTO.<ParticipationRespDTO>withPageInfo()
          .pageParticipationReqDTO(new PageParticipationReqDTO<ParticipationReqDTO>())
          .totalRecords(totalRecordsFromParticipation)
          .respDTOS(participationRespDTOS)
          .statusCountMap(participationCountMap)
          .attendanceRate(attendanceRate)
          .build();
        learner.setPageParticipationRespDTO(pageParticipationRespDTO);

      }
    }
    log.info("total records: " + totalRecords);

    return PageLearnerRespDTO.<LearnerRespDTO>withPageInfo()
      .pageLearnerReqDTO(pageLearnerReqDTO)
      .totalRecords(totalRecords)
      .respDTOS(allLearners)
      .build();




  }

  @Override
  public PageParticipationRespDTO<ParticipationRespDTO> findParticipations(
    PageParticipationReqDTO<ParticipationReqDTO> pageParticipationReqDTO,
    Integer loginUserId,
    String loginUserType,
    Integer leId) {
    Integer totalRecordsFromParticipation = learnerManagementMapper.selecttotalRecordsFromParticipation(leId);

    List<ParticipationRespDTO> participationRespDTOS
      = learnerManagementMapper.selectParticipationsByIdsWithPaging(
        pageParticipationReqDTO,
        loginUserId,
        loginUserType,
        leId
      );
    PageParticipationRespDTO<ParticipationRespDTO> pageParticipationRespDTO
      = PageParticipationRespDTO.<ParticipationRespDTO>withPageInfo()
      .pageParticipationReqDTO(pageParticipationReqDTO)
      .totalRecords(totalRecordsFromParticipation)
      .respDTOS(participationRespDTOS)
      .build();

    return pageParticipationRespDTO;
  }

  @Override
  public Boolean updateEmploymentSupport(Integer loginUserId, String loginUserType, Integer leId, EmploymentSupportReqDTO employmentSupportReqDTO) {
    return learnerManagementMapper.updateEmploymentSupport(loginUserId, loginUserType, leId, employmentSupportReqDTO);
  }

  @Override
  public PageLearnerRespDTO<LearnerRespDTO> getLearnersAllorOne(
      BaseReqDTO baseReqDTO,
      PageLearnerReqDTO<LearnerReqDTO> pageLernerReqDTO) {

    // page정보를 임시 변수에 저장
    Integer originalPageNo = pageLernerReqDTO.getPageNo();
    Integer originalPageSize = pageLernerReqDTO.getPageSize();

    // page정보가 있으면, 임시로 null로 바꿈
    if (originalPageNo != null && originalPageSize != null) {
      pageLernerReqDTO.setPageNo(null);
      pageLernerReqDTO.setPageSize(null);
    }
    // 전체의 레코드 개수를 셈
    List<LearnerRespDTO> allLearners
        = learnerManagementMapper.selectLearnersAllorOne(
            baseReqDTO, pageLernerReqDTO
    );
    Integer totalRecords = allLearners.size();

    // page정보가 있으면, 임시 변수에서 기존의 page 정보를 꺼내어 페이징된 데이터만 조회
    if (originalPageNo != null && originalPageSize != null) {
      pageLernerReqDTO.setPageNo(originalPageNo);
      pageLernerReqDTO.setPageSize(originalPageSize);
      allLearners = learnerManagementMapper.selectLearnersAllorOne(
          baseReqDTO, pageLernerReqDTO
      );
    }

    return PageLearnerRespDTO.<LearnerRespDTO>withPageInfo()
        .pageLearnerReqDTO(pageLernerReqDTO)
        .totalRecords(totalRecords)
        .respDTOS(allLearners)
        .build();
  }





  public PageLearnerResponse<LearnerOverviewResp> getLearnersByAuth(
      BaseReqDTO baseReqDTO,
      PageLearnerRequest<LearnerRequest> pageLearnerRequest
  ) {
    Integer originalPageNo = pageLearnerRequest.getPageNo();
    Integer originalPageSize = pageLearnerRequest.getPageSize();
    if (originalPageNo != null && originalPageSize != null) {
      pageLearnerRequest.setPageNo(null);
      pageLearnerRequest.setPageSize(null);
    }
    log.info("◆baseReqDTO: " + baseReqDTO);
    log.info("◆pageLearnerRequest: " + pageLearnerRequest);
    List<LearnerResponse> learners = learnerManagementMapper.selectLearnersByAuth(
        baseReqDTO, pageLearnerRequest);
    Integer totalRecords = learners.size();

    if (originalPageNo != null && originalPageSize != null) {
      pageLearnerRequest.setPageNo(originalPageNo);
      pageLearnerRequest.setPageSize(originalPageSize);
      learners = learnerManagementMapper.selectLearnersByAuth(
          baseReqDTO, pageLearnerRequest);
    }

    List<LearnerOverviewResp> learnerOverviewResps =
        learners.stream().map(learner -> {

          LearnerOverviewResp resp = new LearnerOverviewResp();

          // 2-1. 사용자 정보 설정
          Optional.ofNullable(learner.getUserId())
              .ifPresent(userId -> resp.setLearnerUser(fetchUser(userId)));

          // 2-2. 수강 등록 정보 설정 (기존 조회 데이터 활용)
          if (learner.getLeId() != null) {
            resp.setLeId(learner.getLeId());
            resp.setLeUserId(learner.getLeUserId());
            resp.setLeCourseId(learner.getLeCourseId());
            resp.setLeCompletionStatus(learner.getLeCompletionStatus());

            // 2-3. 취업 지원 정보
            resp.setLearnerEmploymentSupport(
                fetchEmploymentSupport(learner.getLeId())
            );

            // 2-4. 출석 정보 (1:N)
            resp.setPartOverview(
                fetchParticipationOverview(learner.getLeId())
            );
          }

          // 2-5. 과제 정보 (1:N)
          if (learner.getLeCourseId() != null && learner.getLeUserId() != null) {

            resp.setHomeOverview(
                fetchHomeworkOverview(learner.getLeCourseId(), learner.getLeUserId())
            );

            // 2-6. 시험 정보 (1:N)
            resp.setTestOverview(
                fetchTestOverview(learner.getLeCourseId(), learner.getLeUserId())
            );
          }

          // 2-7. 과정 정보 (배정 포함)
          if (learner.getLeCourseId() != null) {
            CourseWithAssignedInfo course = fetchCourse(learner.getLeCourseId());
            resp.setLearnerCourse(course);
            // 과정 배정 정보 설정 (null-safe)
            if (course != null) {
              Optional.ofNullable(learner.getCoInstructorId())
                  .ifPresent(course::setCoInstructorId);
              Optional.ofNullable(learner.getCoCourseHeadId())
                  .ifPresent(course::setCoCourseHeadId);
              Optional.ofNullable(learner.getCoInstructorName())
                  .ifPresent(course::setCoInstructorName);
              Optional.ofNullable(learner.getCoCourseHeadName())
                  .ifPresent(course::setCoCourseHeadName);
            }
          }

          return resp;

        }).collect(Collectors.toList());

    return PageLearnerResponse.<LearnerOverviewResp>withPageInfo()
        .request(pageLearnerRequest)
        .totalRecords(totalRecords)
        .records(learnerOverviewResps)
        .build();
  }

  @Override
  public String getLoginUserPositionByUserId(Integer loginUserId) {
    return learnerManagementMapper.selectLoginUserPositionByUserId(loginUserId);
  }

  @Override
  public ParticipationWithReason getPartInfoByPid(Integer pid) {
    return learnerManagementMapper.selectPartInfoByPid(pid);
  }


  private User fetchUser(Integer userId) {
    return learnerManagementMapper.selectUserById(userId);
  }
  private EmploymentSupport fetchEmploymentSupport(Integer leId) {
    return learnerManagementMapper.selectEmploymentSupportByLeId(leId);
  }

  private ParticipationOverviewResp<ParticipationWithReason> fetchParticipationOverview(Integer leId) {
    List<ParticipationWithReason> details =
        learnerManagementMapper.selectParticipationByLeId(leId);

    Map<String, Integer> statusCount = details.stream()
        .collect(Collectors.groupingBy(
            ParticipationWithReason::getPartStatus,
            Collectors.summingInt(e -> 1)
        ));

    return ParticipationOverviewResp.<ParticipationWithReason>builder()
        .partList(details)
        .totalCount(details.size())
        .statusCount(statusCount)
        .build();
  }

  private HomeworkOverviewResp<HomeworkWithSubEval> fetchHomeworkOverview(
      Integer leCourseId, Integer leUserId) {

    List<HomeworkWithSubEval> details =
        learnerManagementMapper.selectHomeworkByCourseIdAndUserId(leCourseId, leUserId);

    return HomeworkOverviewResp.<HomeworkWithSubEval>builder()
        .homeList(details)
        .totalCount(details.size())
        .build();
  }

  private TestOverviewResp<TestWithSub> fetchTestOverview(
      Integer leCourseId, Integer leUserId) {
    List<TestWithSub> details =
        learnerManagementMapper.selectTestByCourseIdAndUserId(leCourseId, leUserId);
    return TestOverviewResp.<TestWithSub>builder()
        .testList(details)
        .totalCount(details.size())
        .build();
  }

  private CourseWithAssignedInfo fetchCourse(Integer leCourseId) {
    return learnerManagementMapper.selectCourseByCourseId(leCourseId);
  }

}
