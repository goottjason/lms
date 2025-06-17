package com.goott5.lms.test.service.course;


import com.goott5.lms.test.domain.course.CourseInfoDTO;
import com.goott5.lms.test.mapper.course.TestCourseMapper;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

  private final TestCourseMapper testCourseMapper;


  @Override
  public List<String> getCourseListForAdmin(Boolean isInProgress) {

    return testCourseMapper.selectCourseListForAdmin(isInProgress);
  }

  @Override
  public List<CourseInfoDTO> getCourseListForUser(HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    return testCourseMapper.selectCourseListForUser(loginUser);
  }

}
