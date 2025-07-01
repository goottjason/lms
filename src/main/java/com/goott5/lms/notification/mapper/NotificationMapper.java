package com.goott5.lms.notification.mapper;

import com.goott5.lms.notification.domain.NotificationSaveDTO;
import com.goott5.lms.notification.domain.NotificationVO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper {

  int insertNotification(NotificationSaveDTO notificationSaveDTO);

  @Select("select id, user_id, content, target_uri, is_checked, is_warning, created_at from notification where user_id = #{id} and deleted_at is null order by is_checked, created_at desc")
  List<NotificationVO> selectNotification(UserVO user);

  @Update("update notification set is_checked = 1 where user_id = #{id}")
  void updateIsCheckedByUserId(UserVO loginUser);

  @Select("select * from user u join staff_detail sd on u.id = sd.user_id and sd.leave_date is null where u.type = 'ADMINISTRATOR'")
  List<Integer> selectAdmins();

  @Update("update notification set deleted_at = current_timestamp() where id = #{id}")
  int updateNotificationDeletedAt(int id);

  @Update("update notification set deleted_at = current_timestamp() where user_id = #{id}")
  int updateAllNotificationDeletedAt(UserVO loginUser);

  @Select("select * from user u join staff_detail sd on u.id = sd.user_id and sd.leave_date is null where u.type = 'INSTRUCTOR'")
  List<Integer> selectInstructors();

  @Select("select u.id from user u join learner_enrollment le on u.id = le.user_id join course c on le.course_id = c.id and c.is_in_progress = 1 where u.type = 'LEARNER'")
  List<Integer> selectLearnersInProgress();

  @Select("select u.id from user u join learner_enrollment le on u.id = le.user_id where le.course_id = #{courseId} union select u.id from user u join staff_assignment sa on u.id = sa.user_id where sa.course_id = #{courseId}")
  List<Integer> selectAllInCourse(NotificationSaveDTO notification);

  @Select("select u.id from user u join learner_enrollment le on u.id = le.user_id where le.course_id = #{courseId}")
  List<Integer> selectLearnersInCourse(NotificationSaveDTO notification);

  @Select("select u.id from user u join staff_assignment sa on u.id = sa.user_id and u.type = 'INSTRUCTOR' where sa.course_id = #{courseId}")
  List<Integer> selectInstructorsInCourse(NotificationSaveDTO notification);

  @Select("select u.id from user u join staff_assignment sa on u.id = sa.user_id and u.type = 'ADMINISTRATOR' where sa.course_id = #{courseId}")
  List<Integer> selectAdminsInCourse(NotificationSaveDTO notification);

  @Select("select u.id from user u join learner_enrollment le on u.id = le.user_id join course c on c.id = le.course_id where c.is_in_progress = 1 union select u.id from user u join staff_detail sd on u.id = sd.user_id where sd.leave_date is null")
  List<Integer> selectAllUsers();
}
