package com.goott5.lms.training.controller;

import com.goott5.lms.homework.domain.MyResponseWithDataPYJ;
import com.goott5.lms.homework.service.HomeworkService;
import com.goott5.lms.training.domain.RequestParticipationDTO;
import com.goott5.lms.training.domain.SelectAllTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDTO;
import com.goott5.lms.training.domain.SelectTrainingDetailDTO;
import com.goott5.lms.training.service.TrainingService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/training")
public class TrainingController {

  private final TrainingService trainingService;

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
        trainingDTOList = trainingService.selectTrainingLogForTeacher(loginUser.getId(),decodeCourseName);
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
    if(decodeCourseName != null && !decodeCourseName.isEmpty() && !"".trim().equals(decodeCourseName)) {
      isInProgress = trainingService.isInProgressCourse(decodeCourseName);
      if (!isInProgress) {
        return ResponseEntity.ok(new MyResponseWithDataPYJ(201, "종료한 과정의 trainingLog", resultMap));
      }
    }

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "trainingDTOList with Map", resultMap));
  }

  @GetMapping("/trainingDetail")
  public String trainingDetail(@RequestParam(required = false) Integer trainingId, RequestParticipationDTO requestParticipationDTO,
      SelectTrainingDetailDTO selectTrainingDetailDTO, Model model, HttpSession session) {

    log.info("trainingId: {}, requestParticipationDTO: {}, selectTrainingDetailDTO: {}", trainingId, requestParticipationDTO, selectTrainingDetailDTO);

    try {
      SelectAllTrainingDTO finalSelectTraining = trainingService.selectAllTraining(trainingId, requestParticipationDTO, selectTrainingDetailDTO);
      log.info("finalSelectTraining: {}", finalSelectTraining);
      model.addAttribute("finalSelectTraining", finalSelectTraining);
    } catch (Exception e) {
      log.error("훈련 일지 상세 조회 실패:{}",e.getMessage());
//      return "training/trainingList";
    }

    return "training/trainingDetail";
  }

//  @GetMapping("/trainingLogRegister")
//  public String trainingLogRegister() {
//    return "trainingLog/trainingLogRegister";
//  }

}
