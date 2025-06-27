package com.goott5.lms.coursemodify.service;

import com.goott5.lms.coursemodify.domain.CourseModifyDTO;
import com.goott5.lms.coursemodify.domain.CourseResponseDTO;
import com.goott5.lms.coursemodify.domain.SubjectVO;
import com.goott5.lms.coursemodify.mapper.CourseModifyMapper;
import com.goott5.lms.courseregister.domain.ScheduleDTO;
import com.goott5.lms.courseregister.domain.SubjectDTO;
import com.goott5.lms.courseregister.mapper.CourseRegisterMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CourseModifyServiceImpl implements CourseModifyService {

  private final CourseModifyMapper courseModifyMapper;
  private final CourseRegisterMapper courseRegisterMapper;

  @Override
  public CourseResponseDTO getDetailByCourseId(Integer courseId) {

    CourseResponseDTO courseResponseDTO = courseModifyMapper.selectCourseDetailByCourseId(courseId);

    List<SubjectVO> subjects = courseModifyMapper.selectSubjectsByCourseId(courseId);

    courseResponseDTO.setSubjects(subjects);

    return courseResponseDTO;

  }

  @Override
  public boolean modifyCourse(CourseModifyDTO courseModifyDTO) {

    boolean result = true;

    if(courseModifyMapper.updateCourse(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.updateInstructor(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.updateAdministrator(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.updateOldClassRoom(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.updateClassroomAllocation(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.updateNewClassroom(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.deleteSchedule(courseModifyDTO) < 1){
      result = false;
    }

    if(courseModifyMapper.deleteSubject(courseModifyDTO) < 1){
      result = false;
    }

    // 교과목 정렬
    courseModifyDTO.getSubjects().sort(Comparator.comparingInt(SubjectDTO::getSubjectOrder));

    for (SubjectDTO subjectDTO : courseModifyDTO.getSubjects()) {
      subjectDTO.setCourse_id(courseModifyDTO.getId());
      if(courseRegisterMapper.insertCourseSubject(subjectDTO) < 1){
        result = false;
      };
    }
    log.info("courseSaveDTO : {}", courseModifyDTO);

    // 시간표 Insert
    int remainTime = courseModifyDTO.getTotalHours();
    int subjectIdCount = 0;
    int lunchMinutes = (int) Duration.between(courseModifyDTO.getLunchStartTime(),
            courseModifyDTO.getLunchEndTime()).toMinutes();
    List<ScheduleDTO> scheduleDTOS = new ArrayList<ScheduleDTO>();

    for (LocalDate lessonDay : courseModifyDTO.getLessonDays()) {

      LocalTime startTime = courseModifyDTO.getLessonStartTime();

      for (int i = 1; i <= courseModifyDTO.getDailyHours(); i++) {

        int changeTime = 0;
        for (int j = 0; j <= subjectIdCount; j++) { // 0
          changeTime += courseModifyDTO.getSubjects().get(j).getHours(); // 160
        }

        if (remainTime == courseModifyDTO.getTotalHours() - changeTime) { // 1020-160
          subjectIdCount++;
        }
        int subjectId = courseModifyDTO.getSubjects().get(subjectIdCount).getId();

        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .courseId(courseModifyDTO.getId())
                .subjectId(subjectId)
                .period(i)
                .periodStartTime(startTime)
                .periodEndTime(startTime.plusMinutes(60 - courseModifyDTO.getBreakTime()))
                .classDate(lessonDay)
                .build();

        scheduleDTOS.add(scheduleDTO);

        if (scheduleDTO.getPeriodEndTime().equals(courseModifyDTO.getLunchStartTime())) {
          startTime = startTime.plusMinutes(60 - courseModifyDTO.getBreakTime() + lunchMinutes);
        } else {
          startTime = startTime.plusMinutes(60);
        }

        remainTime--;

      }
    }
    if(courseRegisterMapper.insertCourseSchedule(scheduleDTOS) < 1){
      result = false;
    }

    return result;
  }

}
