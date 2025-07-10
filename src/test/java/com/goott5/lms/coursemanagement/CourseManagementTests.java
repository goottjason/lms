package com.goott5.lms.coursemanagement;

import com.goott5.lms.coursemanagement.domain.dto.PageCourseRequest;
import com.goott5.lms.coursemanagement.domain.dto.PageCourseResponse;
import com.goott5.lms.coursemanagement.domain.integrated.CourseOverviewResp;
import com.goott5.lms.coursemanagement.mapper.CourseManagementMapper;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

@SpringBootTest
@Slf4j
public class CourseManagementTests {
  @Autowired
  private UserService userService;
  @Autowired
  private CourseManagementService courseManagementService;
  @Autowired
  private CourseManagementMapper  courseManagementMapper;

  @Test
  public void signupTest() throws IOException {

    SignupDTO signupDTO = SignupDTO.builder()
        .id(7)
        .loginId("queen1")
        .password("queen!@34")
        .email("ji123@abc.com")
        .mobile("01083839595")
        .address("서울시 노원구 노해로 432")
        .build();

    userService.signup(signupDTO);

  }

  @Test
  public void findUserTest() throws IOException {
    userService.findUserByLoginId("7");
  }

  @Test
  public void insertPartTest() throws IOException {
    BaseReqDTO baseReqDTO = BaseReqDTO.builder()
        .loginUserId(1)
        .loginUserType("ADMINISTRATOR")
        .loginUserPosition("GENERAL_MANAGER")
        .build();
    PageCourseRequest pageCourseRequest = PageCourseRequest.builder()
        .coId(5)
        .build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("referer", "https://example.com/prev-page");
    request.setRequestURI("/test/request-uri");
    PageCourseResponse<CourseOverviewResp> coursesByAuth =
        courseManagementService.getCoursesByAuth(
            baseReqDTO, pageCourseRequest, request);

    log.info("coursesByAuth: {}", coursesByAuth);
    coursesByAuth.getRecords().forEach(course -> {
      int[] values = {16, 17, 18, 19};
      course.getScheduleOverview().getClassdateList().forEach(classDate -> {
        for (int i : values) {
          LocalDateTime checkIn = LocalDateTime.of(classDate.getYear(), classDate.getMonth(), classDate.getDayOfMonth(), 9, 27, 0);
          LocalDateTime checkOut = LocalDateTime.of(classDate.getYear(), classDate.getMonth(), classDate.getDayOfMonth(), 18, 22, 0);
          courseManagementMapper.insertPartTable(
              i, checkIn, checkOut, classDate
          );
        }
      });
    });
  }
}