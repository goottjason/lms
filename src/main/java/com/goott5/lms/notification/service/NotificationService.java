package com.goott5.lms.notification.service;

import com.goott5.lms.notification.domain.NotificationSaveDTO;
import com.goott5.lms.notification.domain.NotificationVO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;

public interface NotificationService {

  boolean saveNotification(NotificationSaveDTO notification);

  List<NotificationVO> getNotification(UserVO user);

  void checkAllNotification(UserVO loginUser);

  List<Integer> sendNotificationToAdmin(NotificationSaveDTO notification);

  boolean deleteNotification(int id);

  boolean deleteAllNotification(UserVO loginUser);

  List<Integer> sendNotificationToInstructor(NotificationSaveDTO notification);

  List<Integer> sendNotificationToLearnerInProgress(NotificationSaveDTO notification);

  List<Integer> sendNotificationToAllInCourse(NotificationSaveDTO notification);

  List<Integer> sendNotificationToLearnerInCourse(NotificationSaveDTO notification);

  List<Integer> sendNotificationToInstructorInCourse(NotificationSaveDTO notification);

  List<Integer> sendNotificationToAdminInCourse(NotificationSaveDTO notification);

  List<Integer> sendNotificationToAll(NotificationSaveDTO notification);
}
