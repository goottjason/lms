package com.goott5.lms.learnermanagement.domain;

import com.goott5.lms.learnermanagement.domain.homework.HomeworkRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.PageParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.participation.ParticipationRespDTO;
import com.goott5.lms.learnermanagement.domain.test.TestRespDTO;
import java.time.LocalTime;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerRespDTO {
  // LearnerEnrollment 테이블 정보
  private Integer leId;
  private String leCompletionStatus;

  // user 테이블 정보
  private Integer userId;
  private String userType;
  private String userLoginId;
  private String userPassword;
  private String userFullname;
  private Character userGender;
  private Date userBirthday;
  private String userMobile;
  private String userEmail;
  private String userAddress;
  private String userProfileImg;
  private String userSessionId;
  private LocalDateTime userAutoLoginLimit;
  private Integer userWrongPasswordCount;
  private LocalDateTime userCreatedAt;
  private LocalDateTime userUpdatedAt;
  private LocalDateTime userDeletedAt;

  // employment_support 테이블 정보
  private Integer esId;
  private Integer esLearnerEnrollmentId;
  private Boolean esIsCounselingReceived;
  private String esCounselingDetails;
  private String esEmploymentStatus;
  private String esCompanyName;
  private String esCompanyPhone;
  private String esCompanyAddress;
  private LocalDateTime esCreatedAt;
  private LocalDateTime esUpdatedAt;
  private LocalDateTime esDeletedAt;

  // 출결정보
  private PageParticipationRespDTO<ParticipationRespDTO> pageParticipationRespDTO;

  // 시험정보
  private List<TestRespDTO> testRespDTOS;

  // 과제정보
  private List<HomeworkRespDTO> homeworkRespDTOS;

  // Course 테이블 정보
  private Integer courseId;
  private String courseName;
  private Boolean courseIsInProgress;
  private LocalTime courseLessonStartTime;
  private LocalTime courseLessonEndTime;
  private String courseFulltimeInstructor;


}
