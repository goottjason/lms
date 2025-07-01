package com.goott5.lms.training.service;

import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import java.util.List;

public interface TrainingService {

  //과정 아이디로 과정명 출력(공통)
  String selectCourseNameById(int courseId);

  // 과정 진행여부 판단(공통)
  boolean isInProgressCourse(String courseName);

  //강사 아이디로 강사 풀네임(공통)
  String selectInstructorNameById(int instructorId);

  //관리자용 select 박스 불러오기
  List<String> selectCourseMenuForAdmin(Boolean isInProgress);

  //강사용 select 박스 불러오기
  List<String> selectCourseMenuForTeacher(int userId);

  //관리자용 trainingLog 불러오기
  List<SelectTrainingDTO> selectTrainingLog(String courseName);

  // 강사용 trainingLog 불러오기
  List<SelectTrainingDTO> selectTrainingLogForTeacher(int instructorId, String courseName);

  //훈련일지 단일 dto 불러오기 (개발 완료까지 필요 없을 시 주석 처리)
  SelectTrainingDTO selectTrainingDTO(int id);

  //훈련 과목 (detail의 plan(int)(=course_schedule id)) 불러오기
  String selectSubjectName(int plan);

  //훈련일지 상세 자료 불러오기
  SelectAllTrainingDTO selectAllTraining(int trainingId, RequestParticipationDTO request, SelectTrainingDetailDTO selectTrainingDetailDTO);


}
