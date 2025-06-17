package com.goott5.lms.learnermanagement.service;

import com.goott5.lms.coursemanagement.mapper.CourseManagementMapper;
import com.goott5.lms.learnermanagement.domain.*;
import com.goott5.lms.learnermanagement.domain.homework.HomeworkRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationReqDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.test.TestRespDTO;
import com.goott5.lms.learnermanagement.mapper.LearnerManagementMapper;

import java.util.HashMap;
import java.util.List;

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
}
