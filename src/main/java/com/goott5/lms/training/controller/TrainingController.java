package com.goott5.lms.training.controller;

import com.goott5.lms.common.util.CreatePOI;
import com.goott5.lms.homework.domain.MyResponseWithDataPYJ;
import com.goott5.lms.training.domain.ExcelRequestDTO;
import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import com.goott5.lms.training.domain.registerdto.InsertFinalRegisterDTO;
import com.goott5.lms.training.domain.registerdto.InsertTrainingDTO;
import com.goott5.lms.training.domain.registerdto.RegisterTrainingParamDTO;
import com.goott5.lms.training.domain.registerdto.SelectAllWithoutActualDTO;
import com.goott5.lms.training.domain.registerdto.SelectCourseDTO;
import com.goott5.lms.training.service.TrainingService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/training")
public class TrainingController {

  private final TrainingService trainingService;
  private final CreatePOI createPOI;
  private static final LocalDateTime IS_REGISTER_TODAY = LocalDateTime.of(LocalDate.now(),LocalTime.of(18,30));

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
  public String training(@RequestParam(required = false) String registerDate,
      RequestParticipationDTO request,
      SelectTrainingDetailDTO selectTrainingDetailDTO, Model model, HttpSession session,
      RedirectAttributes redirectAttributes) {

    //registerDate 존재x
    if (registerDate == null || registerDate.isEmpty()) {
      redirectAttributes.addFlashAttribute("noGet", "등록할 날짜가 존재하지 않습니다.");
      return "redirect:/training/trainingList";
    }

    String decodeRegisterDate = "";
    try {
      log.info("registerDate: {}", URLDecoder.decode(registerDate, "UTF-8")); //받아옴
      decodeRegisterDate = URLDecoder.decode(registerDate, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    model.addAttribute("loginUser", loginUser);

    // 금일 훈련 일지 등록 일자는 금일 퇴실 시간 이후부터
    DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate thisRegisterDate = LocalDate.parse(decodeRegisterDate, dtf);

    if(thisRegisterDate.isEqual(LocalDate.now())){
      if (LocalDateTime.now().isBefore(IS_REGISTER_TODAY)){
        redirectAttributes.addFlashAttribute("noGet", "금일 훈련일지 등록은 18시 30분 이후에 가능합니다.");
        return "redirect:/training/trainingList";
      }
    }

    // 훈련 일지는 금일 이후는 안됨!
    if(thisRegisterDate.isAfter(LocalDate.now())){
      redirectAttributes.addFlashAttribute("noGet", "훈련 일지는 미리 등록할 수 없습니다.");
      return "redirect:/training/trainingList";
    }

    // 공휴일,휴강 제외
    if(trainingService.isHoliday(decodeRegisterDate)){
      redirectAttributes.addFlashAttribute("noGet", "공휴일은 등록할 수 없습니다.");
      return "redirect:/training/trainingList";
    }


    int userId = loginUser.getId();

    SelectAllWithoutActualDTO selectAllWithoutActualDTO =
        trainingService.selectAllWithoutActual(userId, decodeRegisterDate, request,
            selectTrainingDetailDTO);

    log.info("selectAllWithoutActualDTO: {}", selectAllWithoutActualDTO);

    //해당 일자의 훈련일지가 있으면, 등록 막기
    boolean isReRegister = trainingService.isReRegister(decodeRegisterDate, userId);
    if (isReRegister) {
      redirectAttributes.addFlashAttribute("noGet","훈련일지는 두 번 등록 할 수 없습니다.");
      return "redirect:/training/trainingList";
    }

    if (selectAllWithoutActualDTO != null) {
      model.addAttribute("selectAllWithoutActualDTO", selectAllWithoutActualDTO);
    }

    return "training/trainingRegister";
  }


  @PostMapping("/trainingRegister")
  public ResponseEntity<?> trainingRegister(@RequestBody InsertFinalRegisterDTO finalData,
      HttpSession session) {

    log.info("finalData: {}", finalData);

    //로그인한 유저가 해당 과정의 강사인지 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    SelectCourseDTO selectCourseDTO = trainingService.selectCourseDTO(loginUser.getId());

    if (selectCourseDTO == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(500, "당신의 현재 진행 중인 강의가 존재하지 않습니다.", null));
    }

    if (selectCourseDTO.getId() != finalData.getSelectCourseDTO().getId()) {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(401, "권한이 없습니다", null));
    }

    // 문자열 => date로
    String postDate = finalData.getPostDate();
    DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate thisDate = LocalDate.parse(postDate, sdf);

    // 훈련일지 빌드
    InsertTrainingDTO insertTrainingDTO = InsertTrainingDTO.builder()
        .courseId(finalData.getSelectCourseDTO().getId())
        .trainingDate(thisDate)
        .instructorId(loginUser.getId())
        .build();

    int isInsertAll = trainingService.insertTrainingAll(insertTrainingDTO,
        finalData.getDataArray());

    if (isInsertAll == 0 || isInsertAll == -1) {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404, "훈련일지 등록 실패", null));
    }

    //등록 후 해당 훈련일지 id 반환

//    trainingService.insertTrainingAll(insertTrainingDTO,finalData.getDataArray().get(0))

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "등록 완료", isInsertAll));
  }


}
