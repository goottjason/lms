package com.goott5.lms.courseschedule.controller;


import com.goott5.lms.courseschedule.domain.CourseVO;
import com.goott5.lms.courseschedule.domain.ScheduleRequestDTO;
import com.goott5.lms.courseschedule.domain.ScheduleResponseDTO;
import com.goott5.lms.courseschedule.domain.ScheduleVO;
import com.goott5.lms.courseschedule.service.CourseScheduleService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/courseSchedule")
@RequiredArgsConstructor
@Slf4j
public class CourseScheduleController {

  private final CourseScheduleService courseScheduleService;

  @GetMapping("")
  public String courseSchedule() { return "courseManagement/courseSchedule"; }


  @GetMapping("/getCoursesByInProgressAndLoginUser")
  @ResponseBody
  public List<CourseVO> getCoursesByInProgressAndLoginUser(Integer inProgressType, HttpSession session) {

    UserVO loginUser =  (UserVO)session.getAttribute("loginUser");

    return courseScheduleService.getCoursesByInProgressAndLoginUser(inProgressType, loginUser);
  }

  @GetMapping("/getFirstCourseByUser")
  @ResponseBody
  public CourseVO getFirstCourseByUser(HttpSession session) {

    CourseVO result = courseScheduleService.getFirstCourseByUser((UserVO)session.getAttribute("loginUser"));

    return result != null ? result : new CourseVO();
  }

  @GetMapping("/getCourseScheduleByWeekRange")
  @ResponseBody
  public List<ScheduleVO> getCourseScheduleByWeekRange (ScheduleRequestDTO scheduleRequestDTO){

    List<ScheduleVO> scheduleVOS = courseScheduleService.getCourseSchedulesByWeek(scheduleRequestDTO);

    return scheduleVOS;
  }

  @GetMapping("/getCoursesByUser")
  @ResponseBody
  public List<CourseVO> getCoursesByUser(HttpSession session) {

    return courseScheduleService.getCoursesByUser((UserVO)session.getAttribute("loginUser"));
  }

  @GetMapping("/getCourseByIdAndUser")
  @ResponseBody
  public CourseVO getCourseByIdAndUser(int courseId, HttpSession session) {

    UserVO loginUser =  (UserVO)session.getAttribute("loginUser");

    return courseScheduleService.getCourseByIdAndUser(courseId, loginUser);
  }

}
