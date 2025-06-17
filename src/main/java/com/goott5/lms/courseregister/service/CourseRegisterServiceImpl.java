package com.goott5.lms.courseregister.service;

import com.goott5.lms.courseregister.domain.UserVO;
import com.goott5.lms.courseregister.mapper.CourseRegisterMapper;
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
}
