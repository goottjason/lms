package com.goott5.lms.instructorhome.domain;

import com.goott5.lms.courseschedule.domain.ScheduleVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponseDTO {

  private CourseVO course;
  private String classroom;
  private List<EnrolledLearnerVO> enrolledLearnerVOS;
  private int countOfCompletedDays;
  private float progress;
  private int absenceLearnerCount;
  private int inStudyLearnerCount;
  private int notSubmitTestLearnerCount;
  private int notSubmitHomeworkLearnerCount;
  private List<CourseScheduleVO> courseScheduleVOS;
  private boolean isWriteTrainingLogToday;
  private int notEvalHomeworkCount;
  private int notApproveVacationCount;
  private List<CustomTestDTO> customTestDTOS;
  private List<CustomCourseQnAVO> customCourseQnAVOS;
  private List<CustomInquiryVO> customInquiryVOS;
  private List<CustomCourseNoticeVO> customCourseNoticeVOS;
  private List<CustomCourseForumVO> customCourseForumVOS;






}
