package com.goott5.lms.homework.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.homework.domain.*;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.homework.service.HomeworkService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/homework")
public class HomeworkController {

  private final HomeworkService homeworkService;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;

  @Value("${cloud.aws.s3.bucketName}")
  private String bucket;
  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;
  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;
  @Value("${cloud.aws.region.static}")
  private String region;


  @GetMapping("/homeworkList")
  public String homeworkList(Model model, @RequestParam(required = false) Integer pageNo,
      @RequestParam(required = false) String nameForLt,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String order, @RequestParam(required = false) String sortBy,
      @RequestParam(required = false, defaultValue = "") String progress,
      @RequestParam(required = false, defaultValue = "") String nameForAdmin,
      HttpSession session, HttpServletRequest request) {

    if (pageNo == null) {
      pageNo = 1;
    }
    //로그인 여부 확인
    if (session.getAttribute("loginUser") == null) {
      return "redirect:/";
    }

    //로그인한 사용자 타입 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    log.info("loginUser:{}", loginUser);

    String type = "";
    type = loginUser.getType();

    log.info("nameForLt 반영?:{}", nameForLt);
    log.info("order:{}", order);
    log.info("sortBy:{}", sortBy);

    HomeworkRequestDTO homeworkRequestDTO = new HomeworkRequestDTO();
    PagingResponseDTO<HomeworkDTO> pagingResponseDTO = null;
    List<String> menuListForLt = new ArrayList<>();

    //디폴트 nameForLt (현재 듣는 과정의 과제가 디폴트로 출력되게)

    if (nameForLt == null) {
      nameForLt = "";
    }
    if (keyword == null) {
      keyword = "";
    }
    if (order == null || order.trim().isEmpty()) {
      order = "desc"; //추후 프론트에서 설정해주는게 편하면 바꾸기
    }
    if (sortBy == null || sortBy.trim().isEmpty()) {
      sortBy = "endDate"; //추후 프론트에서 설정해주는게 편하면 바꾸기
    }
    if (progress == null) {
      progress = "";
    }
    if (nameForAdmin == null) {
      nameForAdmin = "";
    }

    //조건 적용 전 테스트용-> 조건 적용
    if (!loginUser.getType().equals("ADMINISTRATOR")) {
      //강사,학생 시점
      homeworkRequestDTO = HomeworkRequestDTO.builder()
          .loginId(loginUser.getLoginId())
          .nameForLt(URLDecoder.decode(nameForLt))// 프론트에서 보내기
          .pagingRequest(PagingRequestDTO.builder()
              .pageNo(pageNo)
              .pageSize(5)
              .build())
          .order(order)
          .sortBy(sortBy)
          .keyword(keyword)
          .build();

      pagingResponseDTO = homeworkService.serviceList(homeworkRequestDTO, type);

      // 강사,학생용 메뉴 select
      menuListForLt = homeworkService.selectCourseMenu(loginUser.getLoginId(), type);
      if (menuListForLt == null || menuListForLt.isEmpty()) {
        menuListForLt = new ArrayList<>();
        menuListForLt.add("선택된 강의가 없습니다.");
      }
      model.addAttribute("menuListForLt", menuListForLt); //학생,강사용 메뉴 출력
      model.addAttribute("nameForLt", nameForLt); // 강사, 학생이 속해있는 과정명 select

    } else {
      //관리자 시점
      Boolean isInProgressForAdmin = null;

      if (progress != null && progress.contains("true")) {
        isInProgressForAdmin = true;
      } else if (progress != null && progress.contains("false")) {
        isInProgressForAdmin = false;
      }

      homeworkRequestDTO = HomeworkRequestDTO.builder()
          .loginId(loginUser.getLoginId())
          .nameForAdmin(URLDecoder.decode(nameForAdmin))// 프론트에서 보내기
          .pagingRequest(PagingRequestDTO.builder()
              .pageNo(pageNo)
              .pageSize(5)
              .build())
          .isInProgressForAdmin(isInProgressForAdmin)
          .order(order)
          .sortBy(sortBy)
          .keyword(keyword)
          .build();

      pagingResponseDTO = homeworkService.ServiceAdminList(homeworkRequestDTO);
      log.info("homeworkRequestDTO:{}", homeworkRequestDTO);
      log.info("pagingResponseDTO:{}", pagingResponseDTO);
      log.info("isInProgressForAdmin:{}", isInProgressForAdmin);
      model.addAttribute("isInProgressForAdmin",
          isInProgressForAdmin != null ? isInProgressForAdmin.toString() : "");
      model.addAttribute("nameForAdmin", nameForAdmin);
    }

    String queryString = request.getQueryString(); // 현재 쿼리스트링
    if (queryString == null) {
      queryString = "";
    }

    model.addAttribute("currentQuery", queryString);

    model.addAttribute("loginUser", loginUser);
    model.addAttribute("pagingRequestDTO",
        homeworkRequestDTO.getPagingRequest()); //제게시글에 필요한 파라미터 요청용
    model.addAttribute("pagingResponseDTO", pagingResponseDTO); // 과제 게시글 리스트가 있는 페이징
    model.addAttribute("homeworkList", pagingResponseDTO.getDtoList()); //페이징 dto 안에 있는 과제 리스트

    //(기능 추가) 해당 homeworkId에 로그인한 학생이 제출했는지 여부
    Map<Integer,Boolean> isSubmissionMap = new HashMap<>();
    for (HomeworkDTO homeworkDTO : pagingResponseDTO.getDtoList()) {
      boolean isSubmissionHomework = homeworkService.selectSubmissionLearner(homeworkDTO.getId(),
          loginUser.getId());
      isSubmissionMap.put(homeworkDTO.getId(),isSubmissionHomework);
    }
    if(isSubmissionMap != null && !isSubmissionMap.isEmpty()) {
      model.addAttribute("isSubmissionMap", isSubmissionMap);
    }

    model.addAttribute("order", order); // 강사, 학생이 속해있는 과정명 select
    model.addAttribute("sortBy", sortBy); // 강사, 학생이 속해있는 과정명 select

    //pagingResponseDTO에 따른 과정명 출력
    String courseName = "";
    Map<Integer, String> courseNameMap = new HashMap<>();
    for (HomeworkDTO homeworkDTO : pagingResponseDTO.getDtoList()) {
      courseName = homeworkService.courseNameById(homeworkDTO.getCourseId());
      courseNameMap.put(homeworkDTO.getCourseId(), courseName);
    }
    model.addAttribute("courseNameMap", courseNameMap);

    //homework에 따른 파일 여부 판단 (map:homeworkId = fileList)
    Map<Integer, List<FileSelectDTO>> homeworkFileMap = new HashMap<>();
    pagingResponseDTO.getDtoList().forEach(homeworkDTO -> {
      if (utilService.selectFileList("homework", homeworkDTO.getId()) != null) {
        if (!utilService.selectFileList("homework", homeworkDTO.getId()).isEmpty()) {
          homeworkFileMap.put(homeworkDTO.getId(),
              utilService.selectFileList("homework", homeworkDTO.getId()));
        }
      }
    });
    model.addAttribute("homeworkFileMap", homeworkFileMap);

    return "homework/homeworkList";
  }


  @GetMapping("/homeworkForAdminBoolean")
  @ResponseBody
  public List<String> homeworkForAdminBoolean(@RequestParam(required = false) String progress,
      Model model) {

    // 관리자용 (진행 상황에 따라 과정명 select) -> select 박스 출력 성공시, 로그인 타입에 따라 컨트롤러의 서비스 메서드 구분
    log.info("progress:{}", progress);
    Boolean isInProgressForAdmin = null;

    if (progress != null && progress.contains("true")) {
      isInProgressForAdmin = true;

    } else if (progress != null && progress.contains("false")) {
      isInProgressForAdmin = false;
    }
    log.info("isInProgress:{}", isInProgressForAdmin);

    List<String> menuForAdminName = homeworkService.selectBoxCourseNameForAdmin(
        isInProgressForAdmin);     //시험 삼아 일단 false만 출력-> 추후 select 값에 따라 출력

    if (menuForAdminName == null || menuForAdminName.isEmpty()) {
      menuForAdminName = new ArrayList<>();
      menuForAdminName.add("선택된 강의가 없습니다.");
    }
    log.info("menuForAdminName:{}", menuForAdminName); //여기서 메뉴 select는 출력

    model.addAttribute("isInProgressForAdmin", isInProgressForAdmin);

    return menuForAdminName;
  }


  @GetMapping("/homeworkDetail")
  public String homeworkDetail(@RequestParam(required = false) Integer homeworkId, Model model,
      HttpSession session,RedirectAttributes redirectAttributes) {

    UserVO user = (UserVO) session.getAttribute("loginUser");

    if (user == null) {
      return "redirect:/";
    }

    if (homeworkId == null) {
      redirectAttributes.addFlashAttribute("homeworkIdNull", "해당 과제가 존재하지 않습니다.");
      return "redirect:/homework/alertRedirect";
    }

    // homework 객체 전달
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(homeworkId);
    String courseName = "";

    String instructorLoginId = "";
    if (homeworkDTO != null) {
      model.addAttribute("homework", homeworkDTO);
      //현재 과제가 속해있는 강의명 전달
      courseName = homeworkService.courseNameById(homeworkDTO.getCourseId());
      model.addAttribute("courseName", courseName);
      // 작성자 loginId 전달
      instructorLoginId = homeworkService.selectLoginId(homeworkDTO.getInstructorId());
      if (instructorLoginId != null) {
        model.addAttribute("instructorLoginId", instructorLoginId);
      }
    } else {
      //homeworkDTO가 null(삭제되었을 때)
      redirectAttributes.addFlashAttribute("homeworkIdNull", "해당 과제가 존재하지 않습니다.");
      return "redirect:/homework/alertRedirect";
    }

    // userVO 전달
    model.addAttribute("loginUser", user);
//    model.addAttribute("homeworkId",homeworkId);

    // 파일이 있을 경우, 파일 조회
    List<FileSelectDTO> fileDTOList = utilService.selectFileList("homework", homeworkId);
    if (fileDTOList != null) {
      log.info("fileDTOList:{}", fileDTOList);
      model.addAttribute("fileDTOList", fileDTOList);
    }

    // 조회수 처리
    ReadCountLog readCountLog = ReadCountLog.builder()
        .tableName("homework")
        .tableId(homeworkId)
        .userId(user.getId())
        .build();

    boolean result = homeworkService.updateReadCount(readCountLog);

    if (!result) {
      log.info("조회수 증가에 오류가 있습니다.");
    } else {
      log.info("조회수 업데이트에 성공했습니다.");
    }

    // 지난 과정에 등록하려면 막기
    boolean isSubmissionProgress = homeworkService.isInProgressByHomeworkId(homeworkId);
    model.addAttribute("isSubmissionProgress", isSubmissionProgress);

    return "homework/homeworkDetail";
  }


  @GetMapping("/homeworkRegister")
  public String homeworkRegister(HttpSession session, Model model,
      RedirectAttributes redirectAttributes, @RequestParam(required = false) String courseName) {

    // register 페이지에 접근

    //loginId 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/";
    } else if (!loginUser.getType().equals("INSTRUCTOR")) {
      return "redirect:/homework/homeworkList";
    }

    log.info("courseName:{}", courseName);
    boolean isRegister = true;
    Map<String, Integer> resultMap = new HashMap<>();

    if (courseName != null) {
      // 해당 로그인한 강사의 현재 진행 중인 과정 id와 instructorId를 select(register 시 dto 매핑용)
      resultMap = homeworkService.selectIdCourse(URLDecoder.decode(courseName), loginUser.getId(),
          loginUser.getType());
    }
    log.info("resultMap:{}", resultMap);

    // 선택한 과정이 진행 중 x or 과정명이 선택한 과정 x or 해당 과정이 등록된 유저 아이디가 로그인 유저 아이디x or 유저 타입이 instructor x
    if (resultMap == null || resultMap.get("course_id") == null
        || resultMap.get("user_id") == null) {
      isRegister = false;
      redirectAttributes.addFlashAttribute("isRegister", isRegister);

      return "redirect:/homework/alertRedirect";
    }

    model.addAttribute("courseName", courseName);
    model.addAttribute("resultMap", resultMap);
    model.addAttribute("courseId", resultMap.get("course_id"));
    model.addAttribute("userId", resultMap.get("user_id"));

//    log.info("resultMap:{}",resultMap);
    log.info("user_id:{}", resultMap.get("user_id"));
    log.info("courseId:{}", resultMap.get("course_id"));

    return "homework/homeworkRegister";
  }


  @PostMapping("/homeworkRegister")
  public ResponseEntity<MyResponseWithDataPYJ> insertHomework(
      @Valid @ModelAttribute HomeworkDTO homeworkDTO,
      BindingResult bindingResult, @RequestParam(required = false) MultipartFile[] files,
      Model model) throws IOException {

    log.info("등록한 homeworkDTO:{}", homeworkDTO);

    if (homeworkDTO == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(404, "등록한 과제 전달 실패했습니다.", null));
    }

    //title과 관련된 bindingResultFieldError 추가
    String title = homeworkDTO.getTitle();
    int titleLength = title.getBytes(StandardCharsets.UTF_8).length;
    if (titleLength > 100) {
      bindingResult.addError(new FieldError("homeworkDTO", "title", "100자 이하로 글을 입력해주세요."));
    }else if(title.trim().isEmpty()){
      bindingResult.addError(new FieldError("homeworkDTO", "title", "공백만 쓸 수는 없습니다."));
    }

    //content와 관련된 bindingResultFieldError 추가
    String content = homeworkDTO.getContent();
    if (content == null || content.trim().isBlank()) {
      bindingResult.addError(new FieldError("homeworkDTO", "content", "글자를 입력해주세요."));
    } else {
      int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
      if (contentLength > 1000 || contentLength < 10) {
        bindingResult.addError(
            new FieldError("homeworkDTO", "content", "글자는 10자 이상 1000자 이하여야 합니다."));
      }
    }

    // 제출 기한과 관련된 bindingResult 추가
    //1. 과제 시작 날짜가 now 이상
    LocalDateTime startDate = homeworkDTO.getStartDate();
    if (startDate != null) {
      if (startDate.isBefore(LocalDateTime.now())) {
        bindingResult.addError(new FieldError("homeworkDTO", "startDate", "과제 시작일은 오늘 이후여야 합니다."));
      }
    }

    //과제 마감 날짜가 시작일 이전일 때
    LocalDateTime endDate = homeworkDTO.getEndDate();
    if (endDate != null) {
      if (endDate.isBefore(startDate) || endDate.isEqual(Objects.requireNonNull(startDate))) {
        bindingResult.addError(new FieldError("homeworkDTO", "endDate", "과제 마감일은 시작일 이후여야 합니다."));
      }
    }

    log.info("content.getBytes:{}", content.getBytes(StandardCharsets.UTF_8).length);

    if (bindingResult.hasErrors()) {
      Map<String, String> errorMap = new HashMap<>();

      for (FieldError filedError : bindingResult.getFieldErrors()) {
        errorMap.put(filedError.getField(), filedError.getDefaultMessage());
      }

      //model.addAttribute("errorResultMap",errorMap);
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "필드 에러 발생", errorMap));
    }

    // 게시글 기본 정보 등록(등록시 파일 받아오는 것까지 확인) + 등록한 해당 글의 게시글 id 반환
    int homeworkIdForFile = homeworkService.insertHomework(homeworkDTO);

    if (homeworkIdForFile != -1) {
      log.info("과제 등록 성공:{}", homeworkDTO);
    } else {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "과제 등록 실패", null));
    }

    if (files != null && files.length > 0) {
      //일단 파일 전달 확인
      log.info("requestParam files:{}", Arrays.stream(files).toList());

      for (MultipartFile file : files) {
        // 첨부 파일 서버에 저장 + 경로 저장
        // putObject 뒤에 경로 반환
        String insertPath = s3Uploader.uploadFile("upload/homework", file.getInputStream(),
            file.getOriginalFilename());

        log.info("파일 서버 저장 성공");

        // 받은 파일 dto에 세팅
        // db 에서 해당 테이블의 게시글 id 다시 받아오기
        FileDTO fileDTO = FileDTO.builder()
            .originalName(file.getOriginalFilename())
            .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
            .path(insertPath)
            .size((int) file.getSize())
            .tableName("homework")
            .tableId(homeworkIdForFile)
            .build();

        // 첨부 파일 db에 저장
        int fileInsert = utilService.insertService(fileDTO);
        if (fileInsert == 1) {
          log.info("파일 db에 저장 성공:{}", fileDTO);
        }
      }

    } else {
      log.info("파일 등록 없는 insert 성공");
      return ResponseEntity.ok(
          new MyResponseWithDataPYJ(200, "파일 등록 없는 insert 성공", homeworkIdForFile));
    }

    log.info("파일 등록 with insert 성공");
    return ResponseEntity.ok(
        new MyResponseWithDataPYJ(200, "파일 등록 with insert 성공", homeworkIdForFile));

  }


  @GetMapping("/homeworkModify")
  public String homeworkModify(Model model, @RequestParam(required = false) Integer homeworkId,
      HttpSession session) {

    //해당 게시글의 기존 파일 리스트 조회
    List<FileSelectDTO> beforeFileList = utilService.selectFileList("homework", homeworkId);

    if (beforeFileList != null) {
      if (beforeFileList.size() > 0) {
        model.addAttribute("beforeFileList", beforeFileList);
      }
    }

    //로그인 유저 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/";
    } else if (!loginUser.getType().equals("INSTRUCTOR")) {
      return "redirect:/homework/homeworkList";
    }

    // 해당 과제의 기존 homeworkDTO 불러오기
    //해당 과제의 작성자 id가 로그인한 강사 id와 일치하는지 확인.
    int selectIsInstructor = 0;
    if(loginUser.getLoginId() != null){
      selectIsInstructor = homeworkService.selectIsInstructorId(loginUser.getLoginId(),
          homeworkId); //그냥 login한 아이디가 아닌, id를 바로 가져오는 것을 권장...
    }
    // (유효성 추가(loginUser.getLoginId가 널일 경우))
    int selectIsInstructorByPk = homeworkService.selectIsInstructorIdByPk(loginUser.getId(),homeworkId);

    if (selectIsInstructor == 0 || selectIsInstructorByPk == 0) {
      // 해당 과제의 작성자가 아닌 다른 강사일 경우
      return "redirect:/homework/homeworkList";
    }

    // 해당 homeworkDTO 가져와서 수정해주기
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(homeworkId);
    String courseName = "";
    if (homeworkDTO != null) {
      model.addAttribute("homeworkDTO", homeworkDTO);
      courseName = homeworkService.courseNameById(homeworkDTO.getCourseId());
      model.addAttribute("courseName", courseName);
    }
    model.addAttribute("homeworkId", homeworkId);

    return "homework/homeworkModify";
  }

  @PostMapping("/homeworkModify")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> homeworkModifypost(
      @Valid @ModelAttribute HomeworkModifyDTO homeworkModifyDTO,
      BindingResult bindingResult,
      @RequestParam(value = "files", required = false) List<MultipartFile> files,
      @RequestParam(required = false) List<Integer> deleteFiles,
      @RequestParam(required = false) Integer homeworkId,
      Model model) throws IOException {

    if (homeworkModifyDTO == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(404, "수정할 과제 전달 실패했습니다.", null));
    }

    log.info("전송 받은 homeworkModifyDTO", homeworkModifyDTO);

    if (files != null) {
      log.info("전송 받은 files:{}", files);
    }

    if (deleteFiles != null) {
      log.info("전송 받은 deleteFiles:{}", deleteFiles);
    }

    //title과 관련된 bindingResultFieldError 추가
    String title = homeworkModifyDTO.getTitle();
    int titleLength = title.getBytes(StandardCharsets.UTF_8).length;
    if (titleLength > 100) {
      bindingResult.addError(new FieldError("homeworkModifyDTO", "title", "100자 이하로 제목을 입력해주세요."));
    }else if(title.trim().isEmpty()){
      bindingResult.addError(new FieldError("homeworkModifyDTO", "title", "공백만 쓸 수는 없습니다."));
    }

    //content와 관련된 필드에러 추가
    String content = homeworkModifyDTO.getContent();
    if (content == null || content.trim().isEmpty()) {
      bindingResult.addError(new FieldError("homeworkModifyDTO", "content", "내용을 입력해주세요."));
//        log.info("content 에러:{}", bindingResult.getFieldErrors().get(0).getDefaultMessage());
    } else {
      int length = content.getBytes(StandardCharsets.UTF_8).length;
      if (length < 10 || length > 1000) {
        bindingResult.addError(
            new FieldError("homeworkModifyDTO", "content", "10자 이상 1000자 이하로 입력해주세요."));
      }
    }

    // 제출 기한과 관련된 bindingResult 추가
    LocalDateTime startDate = homeworkModifyDTO.getStartDate();

    //과제 마감 날짜가 시작일 이전일 때(수정일 때만)
    LocalDateTime endDate = homeworkModifyDTO.getEndDate();
    if (endDate != null && startDate != null) {
      if (endDate.isBefore(startDate)) {
        bindingResult.addError(new FieldError("homeworkDTO", "endDate", "과제 마감일은 시작일 이후여야 합니다."));
      }
    }

    // 필드 에러 있을 경우
    if (bindingResult.hasErrors()) {
      Map<String, String> errorMap = new HashMap<>();

      for (FieldError filedError : bindingResult.getFieldErrors()) {
        errorMap.put(filedError.getField(), filedError.getDefaultMessage());
      }
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "필드 에러 발생", errorMap));
    }

    log.info("전송 성공한 homeworkModifyDTO:{}", homeworkModifyDTO);
    log.info("전송 성공한 files:{}", files);
    log.info("전송 성공한 deleteFiles:{}", deleteFiles);
    log.info("homeworkId:{}", homeworkId);

    // 게시글 수정
    homeworkModifyDTO.setId(homeworkId);
    int result = homeworkService.updateHomework(homeworkModifyDTO);

    if (result != 1) {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404, "전송 실패", homeworkModifyDTO));
    }

    // deleteList의 id에 해당하는 파일 dto 가져오기
    List<FileSelectDTO> deleteFileList = new ArrayList<>();
    FileSelectDTO fileSelectDTO = null;

    if (deleteFiles != null) {
      if (!deleteFiles.isEmpty()) {

        for (Integer num : deleteFiles) {
          fileSelectDTO = utilService.selectFileById(num);
          deleteFileList.add(fileSelectDTO);
        }

        log.info("삭제할 deleteFileList:{}", deleteFileList);

        // 해당 파일 dto의 getPath() 또는 지정한 dir와 getName()으로 파일 삭제해보기
        for (FileSelectDTO selectDTO : deleteFileList) {
          log.info("selectDTO.getPath:{}", selectDTO.getPath());
          s3Uploader.deleteFile(
              "upload/homework/" + URLDecoder.decode(selectDTO.getNewName(), "UTF-8"));
          log.info("파일 서버 삭제 성공"); // 성공 못함

          if (utilService.deleteFileById(selectDTO.getId()) == 1) {
            log.info("파일 db 삭제 성공"); // 성공
          }
        }
      }
    }

    // 수정시 생성된 파일
    if (files != null) {
      if (files.size() > 0) {
        for (MultipartFile file : files) {
          // 첨부 파일 서버에 저장 + 경로 저장
          // putObject 뒤에 경로 반환
          String insertPath = s3Uploader.uploadFile("upload/homework", file.getInputStream(),
              file.getOriginalFilename());
          log.info("파일 서버 저장 성공");

          // 받은 파일 dto에 세팅
          // db 에서 해당 테이블의 게시글 id 다시 받아오기
          FileDTO fileDTO = FileDTO.builder()
              .originalName(file.getOriginalFilename())
              .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
              .path(insertPath)
              .size((int) file.getSize())
              .tableName("homework")
              .tableId(homeworkId)
              .build();

          // 첨부 파일 db에 저장
          int fileInsert = utilService.insertService(fileDTO);
          if (fileInsert == 1) {
            log.info("파일 db에 저장 성공:{}", fileDTO);
          }
          ;
        }
      }
    }

    log.info("게시글 수정 성공");
    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "전송 성공", homeworkModifyDTO));

  }


  @DeleteMapping("/deleteHomework")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> deleteHomework(
      @RequestParam(required = false) Integer homeworkId) throws UnsupportedEncodingException {

    if (homeworkId == null) {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "데이터 전송 실패", null));
    }

    // 참조하고 있는 homeworkSubmission이 있으면 삭제 불가
    boolean isSubmission = homeworkService.selectHomeworkSubmissionIdByHomework(homeworkId);
    if (isSubmission) {
      // 존재하면 삭제 x
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(409, "해당 과제의 제출물이 존재합니다.", null));
    }

    // 파일 불러오기
    List<FileSelectDTO> fileList = utilService.selectFileList("homework", homeworkId);

    // 파일 삭제
    if (fileList != null) {
      if (fileList.size() > 0) {
        for (FileSelectDTO selectDTO : fileList) {

          // 해당 파일 dto의 getPath() 또는 지정한 dir와 getName()으로 파일 삭제해보기
          s3Uploader.deleteFile(
              "upload/homework/" + URLDecoder.decode(selectDTO.getNewName(), "UTF-8"));
          log.info("파일 서버 삭제 성공");

          if (utilService.deleteFileById(selectDTO.getId()) == 1) {
            log.info("파일 db 삭제 성공"); // 성공
          }
        }
      }
    }

    // 게시글 삭제
    int deleteNum = homeworkService.deleteHomeworkById(homeworkId);

    if (deleteNum != 1) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(404, "게시글 삭제 실패", homeworkId));
    } else {
      log.info("게시글 삭제 성공");
      return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "게시글 삭제 성공", homeworkId));
    }
  }

  @GetMapping("/alertRedirect")
  public String alertRedirect() {
    return "homework/alertRedirect";
  }

}
