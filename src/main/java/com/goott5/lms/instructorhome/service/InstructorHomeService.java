package com.goott5.lms.instructorhome.service;

import com.goott5.lms.instructorhome.domain.CourseVO;
import com.goott5.lms.instructorhome.domain.CustomCourseVO;
import com.goott5.lms.instructorhome.domain.HomeResponseDTO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;

public interface InstructorHomeService {

  HomeResponseDTO getHomeData(int courseId, UserVO loginUser);

  List<CourseVO> getCourseLists(UserVO loginUser);
}
