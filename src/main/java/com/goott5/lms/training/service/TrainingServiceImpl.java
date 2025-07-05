package com.goott5.lms.training.service;


import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.homework.domain.MyResponseWithDataPYJ;
import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.ResponseParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import com.goott5.lms.training.domain.modifydto.ModifyFinalDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDetailDTO;
import com.goott5.lms.training.domain.registerdto.RegisterTrainingParamDTO;
import com.goott5.lms.training.domain.registerdto.SelectAllWithoutActualDTO;
import com.goott5.lms.training.domain.registerdto.SelectCourseDTO;
import com.goott5.lms.training.domain.registerdto.SelectSchSubDTO;
import com.goott5.lms.training.mapper.TrainingMapper;
import com.goott5.lms.user.domain.UserVO;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

  private final TrainingMapper trainingMapper;
  private final com.goott5.lms.common.mapper.UtilMapper utilMapper;
  private final UtilService utilService;
  private final S3Uploader s3Uploader;
  private static final String FILE_UPLOAD_PATH = "upload/signature/";

  @Override
  public String selectCourseNameById(int courseId) {
    return trainingMapper.selectCourseNameById(courseId);
  }

  @Override
  public boolean isInProgressCourse(String courseName) {
    return trainingMapper.isInProgressCourse(courseName);
  }

  @Override
  public String selectInstructorNameById(int instructorId) {
    return trainingMapper.selectInstructorNameById(instructorId);
  }

  @Override
  public List<String> selectCourseMenuForAdmin(Boolean isInProgress) {
    return trainingMapper.selectBoxCourseNameForAdmin(isInProgress);
  }

  @Override
  public List<String> selectCourseMenuForTeacher(int userId) {
    return trainingMapper.selectCourseMenuForTeacher(userId);
  }

  @Override
  public List<SelectTrainingDTO> selectTrainingLog(String courseName) {
    return trainingMapper.selectTrainingLogForAdmin(courseName);
  }

  @Override
  public List<SelectTrainingDTO> selectTrainingLogForTeacher(int instructorId, String courseName) {
    return trainingMapper.selectTrainingLogForTeacher(instructorId, courseName);
  }

  @Override
  public SelectTrainingDTO selectTrainingDTO(int id) {
    return trainingMapper.selectTrainingLog(id);
  }

  @Override
  public List<Integer> selectAdminIdList(int courseId) {
    return trainingMapper.selectStaffIdByCourseId(courseId);
  }

  @Override
  public String selectSubjectName(int plan) {
    return trainingMapper.selectSubjectName(plan);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SelectAllTrainingDTO selectAllTraining(int trainingId, RequestParticipationDTO request,
      SelectTrainingDetailDTO selectTrainingDetailDTO) {

    SelectAllTrainingDTO finalTraining = null;

    try {
      //id로 selectTrainingDTO 조회
      SelectTrainingDTO selectTrainingDTO = trainingMapper.selectTrainingLog(trainingId);

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
      finalTraining = SelectAllTrainingDTO.builder()
          .selectTrainingDTO(selectTrainingDTO)
          .name(courseName)
          .trainingDate(trainingDate)
          .numberOfLearner(numberOfLearner)
          .responseParticipationDTO(responseParticipationDTO)
          .selectTrainingDetailDTOList(selectTrainingDetailDTOList)
          .build();
    } catch (Exception e) {
      log.error("trainingDetail 조회 실패:{}", e.getMessage());
      throw new RuntimeException(e);
    }

    return finalTraining;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public SelectAllWithoutActualDTO selectAllWithoutActual(int userId, String trainingDate,
      RequestParticipationDTO request, SelectTrainingDetailDTO selectTrainingDetailDTO) {

    SelectCourseDTO selectCourseDTO = trainingMapper.selectCourse(userId);
    log.info("selectCourseDTO={}", selectCourseDTO);

    int learnerNum = trainingMapper.countOfLearner(selectCourseDTO.getId());
    log.info("learnerNum={}", learnerNum);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date thisDate = new Date();
    try {
      thisDate = sdf.parse(String.valueOf(trainingDate)); //문자열 => date
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    //출결 상태에 속하는 학생 수
    Map<String, Integer> participationCountMap = new HashMap<>();
    Map<String, List<String>> participationLearnerMap = new HashMap<>();
    List<String> participationStatus = List.of("ATTENDANCE", "LATE", "ABSENCE", "LEAVE_EARLY",
        "VACATION_PENDING");

    request = RequestParticipationDTO.builder()
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

    List<SelectSchSubDTO> schSubDTOList = trainingMapper.selectSchSub(thisDate, userId);
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

    return selectAllWithoutActualDTO;
  }

  @Override
  public void addFieldErrorsRegister(BindingResult bindingResult, List<RegisterTrainingParamDTO> registerTrainingParamList) {
    if(registerTrainingParamList == null) return;
    for (int i = 0; i < registerTrainingParamList.size(); i++) {
      RegisterTrainingParamDTO registerTrainingParamDTO = registerTrainingParamList.get(i);
      if(registerTrainingParamDTO == null) continue;
      String actual = registerTrainingParamList.get(i).getActual();

      int fieldByte = actual.getBytes(StandardCharsets.UTF_8).length;
      if(registerTrainingParamList.get(i) != null  && fieldByte > 1000){
        bindingResult.addError(new FieldError("finalData","dataArray[" + i + "].actual", "1000자 이상 입력할 수 없습니다."));
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int insertTrainingAll(InsertTrainingDTO insertTrainingDTO,
      List<RegisterTrainingParamDTO> registerParamList) {

    //훈련일지(메인) 넣기
    int insertTraining = trainingMapper.insertTrainingLog(insertTrainingDTO);
    if (insertTraining == 1) {
      log.info("insertTraining={}", insertTraining);
    } else {
      log.warn("insertTraining 실패");
      throw new RuntimeException("insertTraining 실패");
    }

    int lastAutoNum = utilMapper.selectLastIdFromAll();
    if (lastAutoNum == 0 || lastAutoNum == -1) {
      log.warn("최근 insert auto-increment id 가져오기 실패");
      throw new RuntimeException("최근 insert auto-increment id 가져오기 실패");
    }

    log.info("lastAutoNum={}", lastAutoNum);

    for (RegisterTrainingParamDTO registerTrainingParamDTO : registerParamList) {


      //훈련일지 detail insert
      InsertTrainingDetailDTO insertTrainingDetailDTO = InsertTrainingDetailDTO.builder()
          .trainingId(lastAutoNum)
          .period(registerTrainingParamDTO.getPeriod())
          .plan(registerTrainingParamDTO.getPlan())
          .actual(registerTrainingParamDTO.getActual())
          .build();

      int insertTrainingDetail = trainingMapper.insertTrainingDetail(insertTrainingDetailDTO);

      if (insertTrainingDetail == 1) {
        log.info("insertTrainingDetail={}", insertTrainingDetail);
      } else {
        log.warn("insertTrainingDetail 실패");
        throw new RuntimeException("insertTrainingDetail 실패");
      }

    }

    return lastAutoNum;

  }

  @Override
  public SelectCourseDTO selectCourseDTO(int userId) {
    return trainingMapper.selectCourse(userId);
  }

  @Override
  public boolean isHoliday(String trainingDate) {
    return trainingMapper.isHoliday(trainingDate);
  }

  @Override
  public boolean isReRegister(String trainingDate, int instructorId, int courseId) {
    return trainingMapper.isReRegister(trainingDate, instructorId, courseId);
  }

  @Override
  public boolean isRegisterDate(String trainingDate, int courseId) {
    return trainingMapper.isRegisterDate(trainingDate, courseId);
  }

  @Override
  public boolean isMyTrainingLog(int id, int instructorId) {
    return trainingMapper.isMyTrainingLog(id, instructorId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTrainingDetail(Map<String, String> map, int trainingId) {

    for (Entry<String, String> entry : map.entrySet()) {
      try {
        int key = Integer.parseInt(entry.getKey());
        if (trainingMapper.updateTrainingDetail(key,
            (String) entry.getValue()) != 1) {
          throw new RuntimeException("런타임 오류" + key);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(e.getMessage());
      }
    }

    int trainingNum = trainingMapper.updateTrainingLog(trainingId);
    if (trainingNum != 1) {
      throw new RuntimeException("런타임 오류" + trainingNum);
    }

    return true;
  }

  @Override
  public void addFieldErrorsModify(BindingResult bindingResult, ModifyFinalDTO modifyFinalDTO) {
    // modifyFinalDTO의 postMap의 value(actual)이 1000이상이면 막기
    if(modifyFinalDTO == null) return;
    if(modifyFinalDTO.getPostMap() == null) return;
    for(String s :modifyFinalDTO.getPostMap().keySet()){
      if(modifyFinalDTO.getPostMap().get(s).getBytes(StandardCharsets.UTF_8).length > 1000){
        bindingResult.addError(new FieldError("modifyFinalDTO", "modifyFinalDTO.postMap[" + s + "]","1000자 이상 입력할 수 없습니다."));
      }
    }
  }

  @Override
  public boolean canDeleteTraining(int trainingId, int courseId, int userId) {
    // 둘 중에 하나의 검사를 통과 해야 함.

    //1. 강사일 경우
    boolean canTeacher = trainingMapper.isMyTrainingLog(trainingId, userId);

    //2. 관리자일 경우 (해당 과정의)
    boolean canAdmin = trainingMapper.isAdmin(courseId, userId);

    boolean result = canTeacher || canAdmin;

    if (!result) {
      log.info("canTeacher:{}, canAdmin:{}", canTeacher, canAdmin);
    }

    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteTraining(int trainingId, String tableName) {

    // 1. 첨부 파일이 있을 경우 삭제(없으면 건너뛰어도 무방)
    List<FileSelectDTO> fileList = utilMapper.selectFileFrom(tableName, trainingId);

    if (fileList != null && !fileList.isEmpty()) {
      for (FileSelectDTO fileSelectDTO : fileList) {
        // 파일 서버 삭제
        try {
          s3Uploader.deleteFile(FILE_UPLOAD_PATH + fileSelectDTO.getNewName());
        } catch (Exception e) {
          throw new RuntimeException("파일 서버 삭제 실패" + fileSelectDTO.getNewName(), e);
        }
        //파일 db 삭제
        int deleteDBFile = utilService.deleteFileById(fileSelectDTO.getId());
        if (deleteDBFile != 1) {
          throw new RuntimeException("파일 db 삭제 실패");
        }
      }
    } else {
      log.info("fileList is null or empty");
    }

    // 2. 훈련일지 detail 삭제
    int deleteDetail = trainingMapper.deleteTrainingDetail(trainingId);
    if (deleteDetail <= 0) {
      log.info("해당 아이디로 조회된 trainingDetail 존재 x:{}", trainingId);
      throw new RuntimeException("trainingDetail 삭제 실패" + trainingId);
    }

    // 3. 훈련일지  trainingLog 삭제
    int deleteLog = trainingMapper.deleteTrainingLog(trainingId);
    if (deleteLog != 1) {
      log.info("해당 아이디로 조회된 trainingLog 존재 x:{}", trainingId);
      throw new RuntimeException("trainingLog 삭제 실패" + trainingId);
    }

  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MyResponseWithDataPYJ signature(Map<String, String> base64, UserVO loginUser) {

    log.info("base64: {}", base64);
    String base64Str = base64.get("dataURL");
    String pureBase64 = base64Str.split(",")[1];
    log.info("pureBase64: {}", pureBase64);
    log.info("trainingId: {}", base64.get("trainingId"));
    int trainingId = Integer.parseInt(base64.get("trainingId"));

    //파일 이름 생성
    String fileName = loginUser.getLoginId() + "_" + LocalDate.now() + ".png";

    // inputStream 생성
    byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
    InputStream inputStream = new ByteArrayInputStream(imageBytes);

    //dir name
    String dir = "upload/signature";

    // 서명 서버 저장
    String path = "";
    try {
      path = s3Uploader.uploadFile(dir,inputStream,fileName);
      log.info("파일 서버 저장 성공?{}",path);
    } catch (IOException e) {
      log.info("파일 서버 저장 실패{}",path);
      throw new RuntimeException(e);
    }

    //서명 db 저장

    int padding = 0;
    if(pureBase64.endsWith("==")) padding = 2;
    else if(pureBase64.endsWith("=")) padding = 1;

    int size = (int) (pureBase64.length() * 3/4) - padding;

    FileDTO fileDTO = FileDTO.builder()
        .originalName(fileName)
        .newName(path.substring(path.lastIndexOf("/") + 1))
        .path(path)
        .size(size)
        .tableName("training_log")
        .tableId(trainingId)
        .build();

    int dbFileNum = 0;
    try {
      dbFileNum = utilService.insertService(fileDTO);
    } catch (Exception e) {
      log.info("서명 db 저장 실패:{}", dbFileNum);
      throw new RuntimeException("db 저장 실패",e);
    }

    if(dbFileNum <= 0) {
      throw new RuntimeException("db 저장 실패");
    }

    return new MyResponseWithDataPYJ(200,"서명 저장 성공", pureBase64);
  }

  @Override
  public boolean isAdminSignature(int trainingId,  int userId) {
    // trainingId로 훈련일지 조회
    SelectTrainingDTO selectTrainingDTO = trainingMapper.selectTrainingLog(trainingId);

    boolean isAdminSignature = false;
    if (selectTrainingDTO != null) {
      isAdminSignature = trainingMapper.isAdmin(selectTrainingDTO.getCourseId(), userId);

    }

    return isAdminSignature;
  }

  @Override
  public boolean isSuperAdmin(int userId) {
    return userId == 33;
  }

}
