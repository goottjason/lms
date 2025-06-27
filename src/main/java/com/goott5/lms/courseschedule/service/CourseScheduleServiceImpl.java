package com.goott5.lms.courseschedule.service;

import com.goott5.lms.courseschedule.domain.CourseVO;
import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.domain.ScheduleResponseDTO;
import com.goott5.lms.courseschedule.domain.ScheduleVO;
import com.goott5.lms.courseschedule.mapper.CourseScheduleMapper;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseScheduleServiceImpl implements CourseScheduleService {

  private final CourseScheduleMapper courseScheduleMapper;

  @Override
  public List<CourseVO> getCoursesByInProgressAndLoginUser(Integer inProgressType, UserVO loginUser){


    if(isGeneralManager(loginUser)){

      return courseScheduleMapper.selectCoursesByInProgress(inProgressType);
    }

    return courseScheduleMapper.selectCoursesByInProgressAndLoginUser(inProgressType, loginUser);
  }

  @Override
  public CourseVO getFirstCourseByUser(UserVO loginUser) {
    // 제너럴 매니저일 때
    if(isGeneralManager(loginUser)){return courseScheduleMapper.selectFirstCourseInAllCourses();}

    // 그 외
    return courseScheduleMapper.selectFirstCourseByUser(loginUser);
  }

  @Override
  public List<ScheduleVO> getCourseSchedulesByWeek(ScheduleRequestDTO scheduleRequestDTO) {
    return courseScheduleMapper.selectCourseSchedulesByWeek(scheduleRequestDTO);
  }

  @Override
  public List<CourseVO> getCoursesByUser(UserVO loginUser) {
    return courseScheduleMapper.selectcoursesByUser(loginUser);
  }

  @Override
  public CourseVO getCourseByIdAndUser(int courseId, UserVO loginUser) {

    if(isGeneralManager(loginUser)){
      return courseScheduleMapper.selectCourseById(courseId);
    }

    return courseScheduleMapper.selectCourseByIdAndUser(courseId, loginUser);
  }

  private boolean isGeneralManager(UserVO loginUser){

//    if ("LEARNER".equals(loginUser.getType()) || "INSTRUCTOR".equals(loginUser.getType())) {
//      return false;
//    }
    return ("GENERAL_MANAGER".equals(courseScheduleMapper.selectUserPosition(loginUser)));
  }

}
