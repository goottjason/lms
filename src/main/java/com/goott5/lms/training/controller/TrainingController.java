package com.goott5.lms.training.controller;

import com.goott5.lms.common.util.CreatePOI;
import com.goott5.lms.homework.domain.MyResponseWithDataPYJ;
import com.goott5.lms.homework.service.HomeworkService;
import com.goott5.lms.training.domain.ExcelRequestDTO;
import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import com.goott5.lms.training.service.TrainingService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/training")
public class TrainingController {

  private final TrainingService trainingService;
  private final CreatePOI createPOI;

//  private final HomeworkService homeworkService; //공통기능용

  @GetMapping("/trainingList")
  public String trainingList(HttpSession session, Model model) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    model.addAttribute("loginUser", loginUser);

    return "training/trainingList";
  }

  @GetMapping("/getMenuForAdminName")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> getMenuForAdminName(
      @RequestParam(required = false) String stringBoolean) {

    //select 박스 출력
    log.info("stringBoolean: {}", stringBoolean);

    Boolean progressBoolean = null;

    if (stringBoolean != null) {
      if (stringBoolean.equals("true")) {
        progressBoolean = true;
      } else if (stringBoolean.equals("false")) {
        progressBoolean = false;
      }
    }

    log.info("progressBoolean: {}", progressBoolean);

    List<String> menuList = trainingService.selectCourseMenuForAdmin(progressBoolean);

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "관리자 메뉴 select 박스 받아오기 성공", menuList));
  }

  @GetMapping("/getMenuForTeacher")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> getMenuForTeacher(HttpSession session) {

//    log.info("디버깅: {}", "getMenuForTeacher");
    List<String> menuList = new ArrayList<>();
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    log.info("loginUser 디버깅: {}", loginUser);

    if (loginUser != null) {
      menuList = trainingService.selectCourseMenuForTeacher(loginUser.getId());
    } else {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404, "로그인 아이디 x ", null));
    }
//    log.info("teacher menuList 디버깅: {}", menuList);

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "강사 메뉴 select 박스 받아오기 성공", menuList));
  }

  @GetMapping("/getSelectTrainingLog")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> getSelectTrainingLog(HttpSession session,
      @RequestParam(required = false) String courseName) {

    //selectTrainingLog 받아오기
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if (courseName == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(404, "courseName is null", null));
    }

    String decodeCourseName = "";
    try {
      decodeCourseName = URLDecoder.decode(courseName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage());
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404, "디코딩 실패", courseName));
    }

    //리스트의 요소를 순회하며 맵을 만들기
    Map<String, SelectTrainingDTO> resultMap = new HashMap<>();
    List<SelectTrainingDTO> trainingDTOList = new ArrayList<>();

    //관리자일 경우
    if (loginUser != null) {
      if ("INSTRUCTOR".equals(loginUser.getType())) {
        trainingDTOList = trainingService.selectTrainingLogForTeacher(loginUser.getId(),
            decodeCourseName);
      } else {
        trainingDTOList = trainingService.selectTrainingLog(decodeCourseName);
      }
    }

    if (trainingDTOList != null) {
      for (SelectTrainingDTO selectTrainingDTO : trainingDTOList) {
        String courseNameById = trainingService.selectCourseNameById(
            selectTrainingDTO.getCourseId()); //공통용
        log.info("courseNameById:{}", courseNameById); //여기선 잘 받아옴
        resultMap.put(courseNameById, selectTrainingDTO);
      }
    }

    // 과정명이 ""가 아닐 때(전체에 해당 시) 제외 => 해당 과정명의 trainingLog의 진행 여부 판단
    boolean isInProgress = true;
    if (decodeCourseName != null && !decodeCourseName.isEmpty() && !decodeCourseName.trim()
        .isEmpty()) {
      isInProgress = trainingService.isInProgressCourse(decodeCourseName);
      if (!isInProgress) {
        return ResponseEntity.ok(new MyResponseWithDataPYJ(201, "종료한 과정의 trainingLog", resultMap));
      }
    }

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "trainingDTOList with Map", resultMap));
  }

  @GetMapping("/trainingDetail")
  public String trainingDetail(@RequestParam(required = false) Integer trainingId,
      RequestParticipationDTO requestParticipationDTO,
      SelectTrainingDetailDTO selectTrainingDetailDTO, Model model, HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    model.addAttribute("loginUser", loginUser);
    if (loginUser != null) {

      model.addAttribute("loginUserType", loginUser.getType());
    }

    log.info("trainingId: {}, requestParticipationDTO: {}, selectTrainingDetailDTO: {}", trainingId,
        requestParticipationDTO, selectTrainingDetailDTO);

    try {
      SelectAllTrainingDTO finalSelectTraining = trainingService.selectAllTraining(trainingId,
          requestParticipationDTO, selectTrainingDetailDTO);
      log.info("finalSelectTraining: {}", finalSelectTraining);
      model.addAttribute("finalSelectTraining", finalSelectTraining);

      // 훈련 과목 map으로 형성(과목명:training_detail에 쓰일 dto)
      Map<SelectTrainingDetailDTO, String> detailWithSubMap = new TreeMap<>(
          Comparator.comparing(SelectTrainingDetailDTO::getPeriod));

      for (SelectTrainingDetailDTO selectTrainingDetail : finalSelectTraining.getSelectTrainingDetailDTOList()) {
        String subjectName = trainingService.selectSubjectName(selectTrainingDetail.getPlan());
        if (subjectName != null && !subjectName.isEmpty()) {
          detailWithSubMap.put(selectTrainingDetail, subjectName);
        }
      }

      if (!detailWithSubMap.isEmpty()) {
        model.addAttribute("detailWithSubMap", detailWithSubMap);
      }


    } catch (Exception e) {
      log.error("훈련 일지 상세 조회 실패:{}", e.getMessage());
//      return "training/trainingList";
    }

    return "training/trainingDetail";
  }

  @PostMapping("/excel")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> toExcel(
      @RequestBody ExcelRequestDTO excelRequestDTO) {

    log.info("excelRequestDTO: {}", excelRequestDTO); //받아오기 성공

    //빈 맵 형성
    Map<String, Object> realFinalMap = new LinkedHashMap<>();

    // 1. finalSelectTraining
    SelectAllTrainingDTO finalSelectTraining = excelRequestDTO.getFinalSelectTraining();
    log.info("finalSelectTraining: {}", finalSelectTraining);

    //finalSelectTraining => 맵 변환
    Map<String, Object> finalSelectTrainingMap = createPOI.dtoToMap(finalSelectTraining);
    finalSelectTrainingMap.remove("selectTrainingDTO");
    finalSelectTrainingMap.remove("selectTrainingDetailDTOList");
    log.info("finalSelectTrainingMap: {}", finalSelectTrainingMap);

    // 2. selectTrainingDTO
    SelectTrainingDTO selectTrainingDTO = finalSelectTraining.getSelectTrainingDTO();
    log.info("selectTrainingDTO: {}", selectTrainingDTO);

    //selectTrainingDTO => 맵 변환
    Map<String, Object> selectTrainingDTOMap = createPOI.dtoToMap(selectTrainingDTO);
    log.info("selectTrainingDTOMap: {}", selectTrainingDTOMap);

    //3. finalSelectTraining의 selectTrainingDetailDTOList
    Map<String, String> trainingDetailMap = new LinkedHashMap<>();
    Map<String, String> detailMap = excelRequestDTO.getDetailWithSubMap();
    for (String s : detailMap.keySet()) {
      // period만 파싱
      log.info("s: {}", s);
      log.info(s.split("=")[3].split(",")[0]); //period 값
      String period = s.split("=")[3].split(",")[0];
      trainingDetailMap.put(period + "교시", detailMap.get(s));
    }
    log.info("detailMap: {}", detailMap);
    log.info("trainingDetailMap: {}", trainingDetailMap);

    // 실제 상황(plan과 다른)
    Map<String, String> actualPeriodMap = new LinkedHashMap<>();
    for (String s : detailMap.keySet()) {
      String tmp = s.split("=")[5].split(",")[0];
      String period = s.split("=")[3].split(",")[0];
      String actual = tmp.substring(0, tmp.length() - 1);
      actualPeriodMap.put(period + "교시에 실제 한 것", actual);
    }
    log.info("actualPeriodMap: {}", actualPeriodMap);

    //강사명/날짜 넣기
    String instructorName = trainingService.selectInstructorNameById(
        finalSelectTraining.getSelectTrainingDTO().getInstructorId());
    String safeDate = new SimpleDateFormat("yyyy-MM-dd").format(
        finalSelectTraining.getTrainingDate());

    // realFinalMap에 다 넣기
    realFinalMap.put("훈련 일지 아이디", finalSelectTraining.getSelectTrainingDTO().getId()); // 아이디
    realFinalMap.put("과정 아이디", finalSelectTraining.getSelectTrainingDTO().getCourseId()); //과정명
    realFinalMap.put("과정명", finalSelectTraining.getName()); //과정명
    realFinalMap.put("강사명", instructorName);
    realFinalMap.put("총 학생 수", finalSelectTraining.getNumberOfLearner());
    realFinalMap.put("출석생 수",
        finalSelectTraining.getResponseParticipationDTO().getAttendanceCount());
    realFinalMap.put("출석생 리스트",
        finalSelectTraining.getResponseParticipationDTO().getAttendanceList());
    realFinalMap.put("결석생 수", finalSelectTraining.getResponseParticipationDTO().getAbsenceCount());
    realFinalMap.put("결석생 리스트", finalSelectTraining.getResponseParticipationDTO().getAbsenceList());
    realFinalMap.put("지각생 수", finalSelectTraining.getResponseParticipationDTO().getLateCount());
    realFinalMap.put("지각생 리스트", finalSelectTraining.getResponseParticipationDTO().getLateList());
    realFinalMap.put("휴가 학생 수",
        finalSelectTraining.getResponseParticipationDTO().getVacationPaddingCount());
    realFinalMap.put("휴가 학생 리스트",
        finalSelectTraining.getResponseParticipationDTO().getVacationPaddingList());
    realFinalMap.put("조퇴 학생 수",
        finalSelectTraining.getResponseParticipationDTO().getLeaveEarLyCount());
    realFinalMap.put("조퇴 학생 리스트",
        finalSelectTraining.getResponseParticipationDTO().getLeaveEarLyList());
    realFinalMap.put("훈련 날짜", safeDate);
    realFinalMap.putAll(trainingDetailMap);
    realFinalMap.putAll(actualPeriodMap);

    // 훈련일지 파일 생성
    try {
      createPOI.isExcel(safeDate + finalSelectTraining.getName(), realFinalMap);
    } catch (Exception e) {
//      throw new RuntimeException(e);
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(500, "엑셀 생성 실패", e.getMessage()));
    }

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "엑셀 변환 성공", excelRequestDTO));
  }

  @GetMapping("/trainingRegister")
  public String training(@RequestParam(required = false) String registerDate) {

    try {
      log.info("registerDate: {}", URLDecoder.decode(registerDate, "UTF-8")); //받아옴
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    return "training/trainingRegister";
  }


}
