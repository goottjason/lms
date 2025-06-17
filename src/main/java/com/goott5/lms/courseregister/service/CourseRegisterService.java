package com.goott5.lms.courseregister.service;

import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.courseregister.domain.ClassroomVO;
import com.goott5.lms.courseregister.domain.CourseSaveDTO;
import com.goott5.lms.courseregister.domain.UserVO;
import java.util.List;

public interface CourseRegisterService {

  List<UserVO> getNotAssignmentInstructor();

  List<UserVO> getCourseHead();

  List<CancelDateVO> getCancelDates();

  List<ClassroomVO> getClassroom();

  void saveCourse(CourseSaveDTO courseSaveDTO);
}
