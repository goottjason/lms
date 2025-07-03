package com.goott5.lms.instructorhome.service;

import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.mapper.CourseScheduleMapper;
import com.goott5.lms.instructorhome.domain.CustomTestDTO;
import com.goott5.lms.instructorhome.domain.CustomTestSubmissionDTO;
import com.goott5.lms.instructorhome.domain.HomeResponseDTO;
import com.goott5.lms.instructorhome.mapper.InstructorHomeMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class InstructorHomeServiceImpl implements InstructorHomeService {

  private InstructorHomeMapper instructorHomeMapper;
  private CourseScheduleMapper courseScheduleMapper;

  @Override
  public HomeResponseDTO getHomeData(int courseId) {

    HomeResponseDTO homeResponseDTO = new HomeResponseDTO();
    homeResponseDTO.setCourse(instructorHomeMapper.selectCourseById(courseId));

    int countOfCompletedDays = instructorHomeMapper.selectCountOfCompletedDays(courseId, LocalDate.now());
    float progress = Math.round((float) countOfCompletedDays / homeResponseDTO.getCourse().getTotalDays() * 10000) / 100f;
    homeResponseDTO.setProgress(progress);

    homeResponseDTO.setAbsenceLearnerCount(instructorHomeMapper.selectCountOfAbsenceLearnerToday(courseId));
    homeResponseDTO.setInStudyLearnerCount(instructorHomeMapper.selectCountOfInStudyLearnerToday(courseId));
    homeResponseDTO.setNotSubmitTestLearnerCount(instructorHomeMapper.selectCountOfNotSubmitTestLearner(courseId));
    homeResponseDTO.setNotSubmitHomeworkLearnerCount(instructorHomeMapper.setCountOfNotSubmitHomeworkLearner(courseId));

    homeResponseDTO.setScheduleVOS(courseScheduleMapper.selectCourseSchedulesByWeek(makeScheduleRequestDTO(courseId)));

    if(courseScheduleMapper.selectTrainingLogCount(courseId) > 0){
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
      avg = total / customTestDTO.getCustomTestSubmissionDTOS().size();
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







    return homeResponseDTO;
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
