package com.goott5.lms.notification.controller;

import com.goott5.lms.notification.domain.NotificationDeleteDTO;
import com.goott5.lms.notification.domain.NotificationSaveDTO;
import com.goott5.lms.notification.domain.NotificationVO;
import com.goott5.lms.notification.mapper.NotificationMapper;
import com.goott5.lms.notification.service.NotificationService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final SimpMessagingTemplate messagingTemplate;
  private final NotificationService notificationService;


  @PostMapping("/send")
  public void sendNotification(@RequestBody NotificationSaveDTO notification) {

    log.info("Sending notification: {}", notification);

    boolean isSuccess = notificationService.saveNotification(notification);

    if(isSuccess) {
      for(Integer userId : notification.getUserIds()) {

        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");

      }
    }

  }

  @GetMapping("/getNotification")
  public List<NotificationVO> getNotification(HttpSession session) {

    return notificationService.getNotification((UserVO)session.getAttribute("loginUser"));
  }

  @PatchMapping("/checkAllNotification")
  public void checkAllNotification(HttpSession session) {

    notificationService.checkAllNotification((UserVO)session.getAttribute("loginUser"));
  }

  @PatchMapping("/deleteNotification")
  public String deleteNotification(@RequestBody NotificationDeleteDTO notificationDeleteDTO) {

    if(notificationService.deleteNotification(notificationDeleteDTO.getId())){
      return "success";
    } else {
      return "fail";
    }

  }

  @PatchMapping("deleteAllNotification")
  public String deleteAllNotification(HttpSession session) {

    if(notificationService.deleteAllNotification((UserVO)session.getAttribute("loginUser"))){
      return "success";
    } else {
      return "fail";
    }
  }

  @PostMapping("/sendNotificationToAdmin")
  public void sendNotificationToAdmin(@RequestBody NotificationSaveDTO notification) {

    notification.setUserIds(notificationService.sendNotificationToAdmin(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToInstructor")
  public void sendNotificationToInstructor(@RequestBody NotificationSaveDTO notification) {

    notification.setUserIds(notificationService.sendNotificationToInstructor(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToLearnerInProgress")
  public void sendNotificationToLearnerInProgress(@RequestBody NotificationSaveDTO notification) {

    notification.setUserIds(notificationService.sendNotificationToLearnerInProgress(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToAllInCourse")
  public void sendNotificationToAllInCourse(@RequestBody NotificationSaveDTO notification) {


    notification.setUserIds(notificationService.sendNotificationToAllInCourse(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToLearnerInCourse")
  public void sendNotificationToLearnerInCourse(@RequestBody NotificationSaveDTO notification) {


    notification.setUserIds(notificationService.sendNotificationToLearnerInCourse(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToInstructorInCourse")
  public void sendNotificationToInstructorInCourse(@RequestBody NotificationSaveDTO notification) {


    notification.setUserIds(notificationService.sendNotificationToInstructorInCourse(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToAdminInCourse")
  public void sendNotificationToAdminInCourse(@RequestBody NotificationSaveDTO notification) {


    notification.setUserIds(notificationService.sendNotificationToAdminInCourse(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }

  @PostMapping("/sendNotificationToAll")
  public void sendNotificationToAll(@RequestBody NotificationSaveDTO notification) {


    notification.setUserIds(notificationService.sendNotificationToAll(notification));

    if(notification.getUserIds() != null && !notification.getUserIds().isEmpty()){
      for(Integer userId : notification.getUserIds()) {
        messagingTemplate.convertAndSend("/topic/notification/" + userId, "notified");
      }
    }
  }


}
