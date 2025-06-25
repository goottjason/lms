package com.goott5.lms.courseregister.service;

import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.courseregister.domain.ClassroomVO;
import com.goott5.lms.courseregister.domain.CourseSaveDTO;
import com.goott5.lms.courseregister.domain.ScheduleDTO;
import com.goott5.lms.courseregister.domain.SubjectDTO;
import com.goott5.lms.courseregister.domain.UserVO;
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
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseRegisterServiceImpl implements CourseRegisterService {

  private final CourseRegisterMapper courseRegisterMapper;

  @Override
  public List<UserVO> getNotAssignmentInstructor() {
    return courseRegisterMapper.selectNotAssignmentInstructor();
  }

  @Override
  public List<UserVO> getCourseHead() {
    return courseRegisterMapper.selectCourseHead();
  }

  @Override
  public List<CancelDateVO> getCancelDates() {
    return courseRegisterMapper.selectCancelDates();
  }

  @Override
  public List<ClassroomVO> getClassroom() {

    return courseRegisterMapper.selectClassrooms();
  }

  @Override
  public boolean saveCourse(CourseSaveDTO courseSaveDTO) {

    boolean result = true;

    if(courseRegisterMapper.insertCourse(courseSaveDTO) < 1){
      result = false;
    }

    if(courseRegisterMapper.insertStaffAssignment(courseSaveDTO) < 1){
      result = false;
    }
    if(courseRegisterMapper.insertClassroomAllocation(courseSaveDTO) < 1){
      result = false;
    }
    if(courseRegisterMapper.updateClassroom(courseSaveDTO) < 1){
      result = false;
    }

    // 교과목 정렬
    courseSaveDTO.getSubjects().sort(Comparator.comparingInt(SubjectDTO::getSubjectOrder));

    for (SubjectDTO subjectDTO : courseSaveDTO.getSubjects()) {
      subjectDTO.setCourse_id(courseSaveDTO.getId());
      if(courseRegisterMapper.insertCourseSubject(subjectDTO) < 1){
        result = false;
      };
    }
    log.info("courseSaveDTO : {}", courseSaveDTO);

    // 시간표 Insert
    int remainTime = courseSaveDTO.getTotalHours();
    int subjectIdCount = 0;
    int lunchMinutes = (int) Duration.between(courseSaveDTO.getLunchStartTime(),
            courseSaveDTO.getLunchEndTime()).toMinutes();
    List<ScheduleDTO> scheduleDTOS = new ArrayList<ScheduleDTO>();

    for (LocalDate lessonDay : courseSaveDTO.getLessonDays()) {

      LocalTime startTime = courseSaveDTO.getLessonStartTime();

      for (int i = 1; i <= courseSaveDTO.getDailyHours(); i++) {

        int changeTime = 0;
        for (int j = 0; j <= subjectIdCount; j++) { // 0
          changeTime += courseSaveDTO.getSubjects().get(j).getHours(); // 160
        }

        if (remainTime == courseSaveDTO.getTotalHours() - changeTime) { // 1020-160
          subjectIdCount++;
        }
        int subjectId = courseSaveDTO.getSubjects().get(subjectIdCount).getId();

        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                .courseId(courseSaveDTO.getId())
                .subjectId(subjectId)
                .period(i)
                .periodStartTime(startTime)
                .periodEndTime(startTime.plusMinutes(60 - courseSaveDTO.getBreakTime()))
                .classDate(lessonDay)
                .build();

        scheduleDTOS.add(scheduleDTO);

        if (scheduleDTO.getPeriodEndTime().equals(courseSaveDTO.getLunchStartTime())) {
          startTime = startTime.plusMinutes(60 - courseSaveDTO.getBreakTime() + lunchMinutes);
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

  @Override
  public boolean checkNameDuplicate(String name) {

    boolean result = true;

    if(courseRegisterMapper.selectCourseCountByName(name) == 0){
      result = false;
    };

    return result;
  }
}
