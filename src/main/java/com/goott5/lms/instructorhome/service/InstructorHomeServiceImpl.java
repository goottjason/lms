package com.goott5.lms.instructorhome.service;

import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.mapper.CourseScheduleMapper;
import com.goott5.lms.instructorhome.domain.CourseVO;
import com.goott5.lms.instructorhome.domain.CustomCourseVO;
import com.goott5.lms.instructorhome.domain.CustomTestDTO;
import com.goott5.lms.instructorhome.domain.CustomTestSubmissionDTO;
import com.goott5.lms.instructorhome.domain.HomeResponseDTO;
import com.goott5.lms.instructorhome.mapper.InstructorHomeMapper;
import com.goott5.lms.user.domain.UserVO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class InstructorHomeServiceImpl implements InstructorHomeService {

  private final InstructorHomeMapper instructorHomeMapper;

  @Override
  public HomeResponseDTO getHomeData(int courseId, UserVO loginUser) {

    HomeResponseDTO homeResponseDTO = new HomeResponseDTO();
    homeResponseDTO.setCourse(instructorHomeMapper.selectCourseById(courseId));
    homeResponseDTO.setClassroom(instructorHomeMapper.selectClassroom(courseId));
    homeResponseDTO.setEnrolledLearnerVOS(instructorHomeMapper.selectEnrolledLearners(courseId));


    int countOfCompletedDays = instructorHomeMapper.selectCountOfCompletedDays(courseId, LocalDate.now());
    homeResponseDTO.setCountOfCompletedDays(countOfCompletedDays);
    float progress = Math.round((float) countOfCompletedDays / homeResponseDTO.getCourse().getTotalDays() * 10000) / 100f;
    homeResponseDTO.setProgress(progress);

    homeResponseDTO.setAbsenceLearnerCount(instructorHomeMapper.selectCountOfAbsenceLearnerToday(courseId));
    homeResponseDTO.setInStudyLearnerCount(instructorHomeMapper.selectCountOfInStudyLearnerToday(courseId));
    homeResponseDTO.setNotSubmitTestLearnerCount(instructorHomeMapper.selectCountOfNotSubmitTestLearner(courseId));
    homeResponseDTO.setNotSubmitHomeworkLearnerCount(instructorHomeMapper.setCountOfNotSubmitHomeworkLearner(courseId));

    homeResponseDTO.setCourseScheduleVOS(instructorHomeMapper.selectCourseSchedules(LocalDate.now(), courseId));

    if(instructorHomeMapper.selectTrainingLogCount(courseId) > 0){
      homeResponseDTO.setWriteTrainingLogToday(true);
    } else {
      homeResponseDTO.setWriteTrainingLogToday(false);
    }

    homeResponseDTO.setNotEvalHomeworkCount(instructorHomeMapper.selectCountOfNotEvalHomework(courseId));
    homeResponseDTO.setNotApproveVacationCount(instructorHomeMapper.selectCountOfNotApproveVacation(courseId));

    // 시험 데이터 받아오기

    homeResponseDTO.setCustomTestDTOS(instructorHomeMapper.selectTestByCourseId(courseId));
    for(CustomTestDTO customTestDTO : homeResponseDTO.getCustomTestDTOS()){
      customTestDTO.setCustomTestSubmissionDTOS(instructorHomeMapper.selectTestSubmission(customTestDTO));
      float avg = 0;
      int total = 0;
      double sumOfSquares = 0;

      for(CustomTestSubmissionDTO customTestSubmissionDTO : customTestDTO.getCustomTestSubmissionDTOS()){
        total += customTestSubmissionDTO.getScore();
      }
      avg = total / (float)customTestDTO.getCustomTestSubmissionDTOS().size();
      customTestDTO.setAverage(avg);
      for(CustomTestSubmissionDTO customTestSubmissionDTO : customTestDTO.getCustomTestSubmissionDTOS()){
        sumOfSquares += Math.pow(customTestSubmissionDTO.getScore() - avg, 2);
      }
      if(customTestDTO.getCustomTestSubmissionDTOS().size() <= 1){
        continue;
      }
      double variance = sumOfSquares / customTestDTO.getCustomTestSubmissionDTOS().size() - 1;
      customTestDTO.setVariance(Math.round(variance * 100) / 100f);

      double standardDeviation = Math.sqrt(variance);
      customTestDTO.setStandardDeviation(Math.round(standardDeviation * 100) / 100f);
    }

    homeResponseDTO.setCustomCourseQnAVOS(instructorHomeMapper.selectCourseQnA(courseId));
    homeResponseDTO.setCustomInquiryVOS(instructorHomeMapper.selectInquiry(loginUser));
    homeResponseDTO.setCustomCourseNoticeVOS(instructorHomeMapper.selectCourseNotice(courseId));
    homeResponseDTO.setCustomCourseForumVOS(instructorHomeMapper.selectCourseForum(courseId));

    return homeResponseDTO;
  }

  @Override
  public List<CourseVO> getCourseLists(UserVO loginUser) {
    return instructorHomeMapper.selectCourseLists(loginUser);
  }


  private ScheduleRequestDTO makeScheduleRequestDTO(int courseId){
    LocalDate today = LocalDate.now();

    // 오늘이 무슨 요일인지 (1=월, 7=일)
    DayOfWeek dayOfWeek = today.getDayOfWeek();

    int dayValue = dayOfWeek.getValue();
    int daysSinceSunday = dayValue % 7; // 일요일이면 0

    LocalDate sunday = today.minusDays(daysSinceSunday);

    LocalDate monday = sunday.plusDays(1);
    LocalDate friday = sunday.plusDays(5);

    return ScheduleRequestDTO.builder()
            .courseId(courseId)
            .weekStart(monday)
            .weekEnd(friday)
            .build();


  }
}
