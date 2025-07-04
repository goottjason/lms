package com.goott5.lms.training;


import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.ResponseParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDetailDTO;
import com.goott5.lms.training.domain.registerdto.SelectAllWithoutActualDTO;
import com.goott5.lms.training.domain.registerdto.SelectCourseDTO;
import com.goott5.lms.training.domain.registerdto.SelectSchSubDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import com.goott5.lms.training.mapper.TrainingMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrainingTest {


  @Autowired(required = true)
  TrainingMapper trainingMapper;

  @Autowired(required = true)
  UtilMapper utilMapper;

  @Test
  public void selectTrainingTest() {

    List<String> courseNameForAdmin = trainingMapper.selectBoxCourseNameForAdmin(true);

    if (courseNameForAdmin != null) {
      for (String s : courseNameForAdmin) {
        log.info("courseNameForAdmin={}", s);

      }
    }
  }

  @Test
  public void selectTrainingLogTest() {
    List<SelectTrainingDTO> list = trainingMapper.selectTrainingLogForAdmin(null);

    if (list != null) {
      for (SelectTrainingDTO dto : list) {
        log.info("dto={}", dto);
      }
    }
  }

  @Test
  public void selectTrainingLogForTeacherTest() {
    List<SelectTrainingDTO> list = trainingMapper.selectTrainingLogForTeacher(35, "");

    if (list != null) {
      for (SelectTrainingDTO dto : list) {
        log.info("dto={}", dto);
      }
    }
  }

  @Test
  public void isInProgressTest() {
    boolean isInProgress = trainingMapper.isInProgressCourse("[1회차] 자바 단기특강");
    if (isInProgress) {
      log.info("진행o={}", isInProgress);
    } else {
      log.info("진행x={}", isInProgress);
    }
  }

  @Test
  public void selectTrainingLog() {
    SelectTrainingDTO selectTrainingDTO = trainingMapper.selectTrainingLog(2);

    List<SelectTrainingDetailDTO> detailDTOList = trainingMapper.selectTrainingDetail(
        selectTrainingDTO.getId());

    log.info("selectTrainingDTO={}", selectTrainingDTO);
    for (SelectTrainingDetailDTO selectTrainingDetailDTO : detailDTOList) {
      log.info("selectTrainingDetailDTO={}", selectTrainingDetailDTO);
    }

    RequestParticipationDTO requestParticipationDTO = RequestParticipationDTO.builder()
        .status("ABSENCE")
        .trainingDate(selectTrainingDTO.getTrainingDate())
        .courseId(selectTrainingDTO.getCourseId())
        .build();

    int countNum = trainingMapper.countOfLearner(selectTrainingDTO.getCourseId());
    log.info("총 학생 수={}", countNum);

    int selectNumber = trainingMapper.countParticipation(requestParticipationDTO);
    List<String> selectList = trainingMapper.listParticipationLearner(requestParticipationDTO);

    log.info("해당 학생 리스트={}", selectList);
    log.info("해당 학생 수={}", selectNumber);

  }

  @Test
  public void selectTrainingLogOne() {
    SelectTrainingDTO selectTrainingDTO = trainingMapper.selectTrainingLog(2);
    log.info("selectTrainingDTO={}", selectTrainingDTO);
  }

  @Test
  @Transactional
  public void testDetailRead() {
    SelectTrainingDTO selectTrainingDTO = trainingMapper.selectTrainingLog(2);

    RequestParticipationDTO request = RequestParticipationDTO.builder()
        .trainingDate(selectTrainingDTO.getTrainingDate())
        .courseId(selectTrainingDTO.getCourseId())
        .build();

    // 해당 trainingDTO의 과정 아이디로 과정명 조회
    String courseName = trainingMapper.selectCourseNameById(selectTrainingDTO.getCourseId());

    //해당 trainingDTO의 훈련 일자
    Date trainingDate = selectTrainingDTO.getTrainingDate();

    // 해당 trainingDTO의 과정의 총 수강생 수
    int numberOfLearner = trainingMapper.countOfLearner(selectTrainingDTO.getCourseId());

    //RequestParticipationDTO 빌드(초기화)
    request = RequestParticipationDTO.builder()
        .status("")
        .trainingDate(trainingDate)
        .courseId(selectTrainingDTO.getCourseId())
        .build();

    //출결 상태에 속하는 학생 수
    Map<String, Integer> participationCountMap = new HashMap<>();
    Map<String, List<String>> participationLearnerMap = new HashMap<>();
    List<String> participationStatus = List.of("ATTENDANCE", "LATE", "ABSENCE", "LEAVE_EARLY",
        "VACATION_PENDING");

    for (String status : participationStatus) {
      request = RequestParticipationDTO.builder()
          .status(status)
          .trainingDate(trainingDate)
          .courseId(selectTrainingDTO.getCourseId())
          .build();
      int count = trainingMapper.countParticipation(request);
      participationCountMap.put(status, count);
      List<String> learnerList = trainingMapper.listParticipationLearner(request);
      participationLearnerMap.put(status, learnerList);
    }

    //출결 현황 모두 ResponseParticipationDTO에 세팅
    ResponseParticipationDTO responseParticipationDTO = ResponseParticipationDTO.builder()
        .attendanceCount(participationCountMap.get("ATTENDANCE")) //출석
        .attendanceList(participationLearnerMap.get("ATTENDANCE"))
        .lateCount(participationCountMap.get("LATE")) //지각
        .lateList(participationLearnerMap.get("LATE"))
        .absenceCount(participationCountMap.get("ABSENCE")) // 결석
        .absenceList(participationLearnerMap.get("ABSENCE"))
        .leaveEarLyCount(participationCountMap.get("LEAVE_EARLY")) // 조퇴
        .leaveEarLyList(participationLearnerMap.get("LEAVE_EARLY"))
        .vacationPaddingCount(participationCountMap.get("VACATION_PENDING")) // 휴가
        .vacationPaddingList(participationLearnerMap.get("VACATION_PENDING"))
        .build();

    //훈련일지 아이디로 해당 훈련일지 detailList 모두 받아오기
    List<SelectTrainingDetailDTO> selectTrainingDetailDTOList = trainingMapper.selectTrainingDetail(
        selectTrainingDTO.getId());

    // SelectAllTrainingDTO에 최종으로 넣기
    SelectAllTrainingDTO finalTraining = SelectAllTrainingDTO.builder()
        .selectTrainingDTO(selectTrainingDTO)
        .name(courseName)
        .trainingDate(trainingDate)
        .numberOfLearner(numberOfLearner)
        .responseParticipationDTO(responseParticipationDTO)
        .selectTrainingDetailDTOList(selectTrainingDetailDTOList)
        .build();

    log.info("finalTraining={}", finalTraining);

  }

  @Test
  public void subjectName() {

    String subjectName = trainingMapper.selectSubjectName(7375);

    log.info("subjectName={}", subjectName);

  }

  @Test
  public void selectCourseById() {

    SelectCourseDTO selectCourseDTO = trainingMapper.selectCourse(35);
    log.info("selectCourseDTO={}", selectCourseDTO);

    int learnerNum = trainingMapper.countOfLearner(selectCourseDTO.getId());
    log.info("learnerNum={}", learnerNum);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date thisDate = new Date();
    try {
      thisDate = sdf.parse("2025-06-23");
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    //출결 상태에 속하는 학생 수
    Map<String, Integer> participationCountMap = new HashMap<>();
    Map<String, List<String>> participationLearnerMap = new HashMap<>();
    List<String> participationStatus = List.of("ATTENDANCE", "LATE", "ABSENCE", "LEAVE_EARLY",
        "VACATION_PENDING");

    RequestParticipationDTO request = RequestParticipationDTO.builder()
        .status("")
        .trainingDate(thisDate)
        .courseId(selectCourseDTO.getId())
        .build();

    for (String status : participationStatus) {
      request = RequestParticipationDTO.builder()
          .status(status)
          .trainingDate(thisDate)
          .courseId(selectCourseDTO.getId())
          .build();
      int count = trainingMapper.countParticipation(request);
      participationCountMap.put(status, count);
      List<String> learnerList = trainingMapper.listParticipationLearner(request);
      participationLearnerMap.put(status, learnerList);
    }

    //출결 현황 모두 ResponseParticipationDTO에 세팅
    ResponseParticipationDTO responseParticipationDTO = ResponseParticipationDTO.builder()
        .attendanceCount(participationCountMap.get("ATTENDANCE")) //출석
        .attendanceList(participationLearnerMap.get("ATTENDANCE"))
        .lateCount(participationCountMap.get("LATE")) //지각
        .lateList(participationLearnerMap.get("LATE"))
        .absenceCount(participationCountMap.get("ABSENCE")) // 결석
        .absenceList(participationLearnerMap.get("ABSENCE"))
        .leaveEarLyCount(participationCountMap.get("LEAVE_EARLY")) // 조퇴
        .leaveEarLyList(participationLearnerMap.get("LEAVE_EARLY"))
        .vacationPaddingCount(participationCountMap.get("VACATION_PENDING")) // 휴가
        .vacationPaddingList(participationLearnerMap.get("VACATION_PENDING"))
        .build();

    log.info("responseParticipationDTO={}", responseParticipationDTO);

    List<SelectSchSubDTO> schSubDTOList = trainingMapper.selectSchSub(thisDate, 35);
    for (SelectSchSubDTO selectSchSubDTO : schSubDTOList) {
      log.info("selectSchSubDTO={}", selectSchSubDTO);
    }

    SelectAllWithoutActualDTO selectAllWithoutActualDTO = SelectAllWithoutActualDTO.builder()
        .selectCourseDTO(selectCourseDTO)
        .trainingDate(thisDate)
        .numberOfLearner(learnerNum)
        .responseParticipationDTO(responseParticipationDTO)
        .selectSchSubDTOList(schSubDTOList)
        .build();

    log.info("selectAllWithoutActualDTO={}", selectAllWithoutActualDTO);



  }

  @Test
  @Transactional
  public void selectCourseByIdForTeacher(){

    LocalDate date = LocalDate.now();

    InsertTrainingDTO insertTrainingDTO = InsertTrainingDTO.builder()
        .courseId(38)
        .trainingDate(date)
        .instructorId(35)
        .build();

    int insertTraining = trainingMapper.insertTrainingLog(insertTrainingDTO);
    if(insertTraining > 0){
      log.info("insertTraining={}", insertTraining);
    }else{
      log.info("insertTraining 실패");
    }

    int lastAutoNum = utilMapper.selectLastIdFromAll();

    log.info("lastAutoNum={}", lastAutoNum);

    InsertTrainingDetailDTO insertTrainingDetailDTO = InsertTrainingDetailDTO.builder()
        .trainingId(lastAutoNum)
        .period(1)
        .plan(6297)
        .build();

    int insertTraingDetail = trainingMapper.insertTrainingDetail(insertTrainingDetailDTO);

    if(insertTraingDetail == 1){
      log.info("insertTrainingDetail={}", insertTraingDetail);
    }else{
      log.info("insertTrainingDetail 실패");
    }

  }

  @Test
  @Transactional
  public void selectCourseByIdForAdmin(){
    boolean isHoliday = trainingMapper.isHoliday("2025-07-25");

    log.info("isHoliday={}", isHoliday); //false

  }

  @Test
  @Transactional
  public void updateTrainingLogDetail(){
    int updateNum = trainingMapper.updateTrainingDetail(17,"진도 빠름");
    if(updateNum == 1){
      log.info("updateNum={}", updateNum);
    }else{
      log.info("업데이트 실패");
    }
  }

  @Test
  @Transactional
  public void isYourAdmin(){
//    boolean isAdmin = trainingMapper.isAdmin(37,37);
    // 둘 중에 하나의 검사를 통과 해야 함.

    //1. 강사일 경우
    boolean canTeacher = trainingMapper.isMyTrainingLog(1,34);


    //2. 관리자일 경우 (해당 과정의)
    boolean canAdmin = trainingMapper.isAdmin(37,37);

    boolean result = canTeacher || canAdmin;

    if(!result){
      log.info("canTeacher:{}, canAdmin:{}",canTeacher,canAdmin);
    }else{
      log.info("유효성 통과");
    }
  }

  @Test
  @Transactional
  public void isRegisterCan(){
    boolean isRegister = trainingMapper.isRegisterDate("2025-06-30", 63);
    log.info("isRegister={}", isRegister);
  }



}
