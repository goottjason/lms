package com.goott5.lms.notification.service;

import com.goott5.lms.notification.domain.NotificationSaveDTO;
import com.goott5.lms.notification.domain.NotificationVO;
import com.goott5.lms.notification.mapper.NotificationMapper;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationMapper notificationMapper;

  @Override
  public boolean saveNotification(NotificationSaveDTO notificationSaveDTO) {



    return (notificationMapper.insertNotification(notificationSaveDTO) > 0);
  }

  @Override
  public List<NotificationVO> getNotification(UserVO user) {
    return notificationMapper.selectNotification(user);
  }

  @Override
  public void checkAllNotification(UserVO loginUser) {
    notificationMapper.updateIsCheckedByUserId(loginUser);
  }

  @Override
  public boolean deleteNotification(int id) {

    if(notificationMapper.updateNotificationDeletedAt(id) > 0){
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean deleteAllNotification(UserVO loginUser) {

    if(notificationMapper.updateAllNotificationDeletedAt(loginUser) > 0){
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<Integer> sendNotificationToInstructor(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectInstructors();

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToLearnerInProgress(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectLearnersInProgress();

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToAllInCourse(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectAllInCourse(notification);

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToLearnerInCourse(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectLearnersInCourse(notification);

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToInstructorInCourse(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectInstructorsInCourse(notification);

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToAdminInCourse(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectAdminsInCourse(notification);

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToAll(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectAllUsers();

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }

  @Override
  public List<Integer> sendNotificationToAdmin(NotificationSaveDTO notification) {

    List<Integer> userIds = notificationMapper.selectAdmins();

    notification.setUserIds(userIds);

    notificationMapper.insertNotification(notification);

    return userIds;
  }
}
