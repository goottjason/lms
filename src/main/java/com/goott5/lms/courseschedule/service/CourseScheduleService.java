package com.goott5.lms.courseschedule.service;


import com.goott5.lms.courseschedule.domain.CourseVO;
import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.domain.ScheduleResponseDTO;
import com.goott5.lms.courseschedule.domain.ScheduleVO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;

public interface CourseScheduleService {

  List<CourseVO> getCoursesByInProgressAndLoginUser(Integer inProgressType, UserVO loginUser);

  CourseVO getFirstCourseByUser(UserVO loginUser);

  List<ScheduleVO> getCourseSchedulesByWeek(ScheduleRequestDTO scheduleRequestDTO);

  List<CourseVO> getCoursesByUser(UserVO loginUser);
}
