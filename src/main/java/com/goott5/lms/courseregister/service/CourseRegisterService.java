package com.goott5.lms.courseregister.service;

import com.goott5.lms.courseregister.domain.UserVO;
import java.util.List;

public interface CourseRegisterService {

  List<UserVO> getNotAssignmentInstructor();

  List<UserVO> getCourseHead();
}
