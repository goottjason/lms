package com.goott5.lms.courseregister.mapper;

import com.goott5.lms.courseregister.domain.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseRegisterMapper {


  @Select("select id, fullname from user where type = 'INSTRUCTOR' and id "
          + "not in (select u.id from staff_assignment sa left join course c on sa.course_id = c.id  left join user u on sa.user_id = u.id "
          + "where c.is_in_progress = 1 and u.type = 'INSTRUCTOR')")
  List<UserVO> selectNotAssignmentInstructor();

  @Select("select u.id, u.fullname from user u join staff_detail sd on u.id = sd.user_id where position = 'course_head'")
  List<UserVO> selectCourseHead();
}
