package com.goott5.lms.test.mapper.course;


import com.goott5.lms.test.domain.course.CourseInfoDTO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper {

  // 관리자 강좌 리스트 가져오기
  List<String> selectCourseListForAdmin(Boolean isInProgress);

  // 수강생, 강사 강좌 리스트 가져오기
  List<CourseInfoDTO> selectCourseListForUser(UserVO loginUser);
}
