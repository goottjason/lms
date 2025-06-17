package com.goott5.lms.homework.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.homework.domain.*;
import com.goott5.lms.homework.readcountlog.domain.ReadCountLog;
import com.goott5.lms.homework.service.HomeworkService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
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
  private final UtilMapper utilMapper;

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
      @RequestParam(required = false) String nameForLt, @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String order, @RequestParam(required = false) String sortBy,
      @RequestParam(required = false, defaultValue = "") String progress, @RequestParam(required = false, defaultValue = "") String nameForAdmin,
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


      //조건 적용 전 테스트용-> 조건 적용
    if(!loginUser.getType().equals("ADMINISTRATOR")){
      //강사,학생 시점
    homeworkRequestDTO = HomeworkRequestDTO.builder()
        .loginId(loginUser.getLoginId())
        .nameForLt(nameForLt)// 프론트에서 보내기
        .pagingRequest(PagingRequestDTO.builder()
            .pageNo(pageNo)
            .pageSize(5)
            .build())
        .order(order)
        .sortBy(sortBy)
        .keyword(keyword)
        .build();

    pagingResponseDTO = homeworkService.serviceList(homeworkRequestDTO,type);

      // 강사,학생용 메뉴 select
      menuListForLt = homeworkService.selectCourseMenu(loginUser.getLoginId(),type);
      if(menuListForLt == null || menuListForLt.isEmpty()){
        menuListForLt = new ArrayList<>();
        menuListForLt.add("선택된 강의가 없습니다.");
      }
      model.addAttribute("menuListForLt",menuListForLt); //학생,강사용 메뉴 출력
      model.addAttribute("nameForLt",nameForLt); // 강사, 학생이 속해있는 과정명 select

    } else {
      //관리자 시점
      Boolean isInProgressForAdmin = null;

      if(progress != null && progress.contains("true")){
        isInProgressForAdmin = true;
      } else if (progress != null && progress.contains("false")) {
        isInProgressForAdmin = false;
      }


      homeworkRequestDTO = HomeworkRequestDTO.builder()
          .loginId(loginUser.getLoginId())
          .nameForAdmin(nameForAdmin)// 프론트에서 보내기
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
      model.addAttribute("isInProgressForAdmin", isInProgressForAdmin != null ? isInProgressForAdmin.toString() : "");
      model.addAttribute("nameForAdmin", nameForAdmin);
    }

    String queryString = request.getQueryString(); // 현재 쿼리스트링
    if (queryString == null) queryString = "";

    model.addAttribute("currentQuery", queryString);


    model.addAttribute("loginUser",loginUser);
    model.addAttribute("pagingRequestDTO", homeworkRequestDTO.getPagingRequest()); //제게시글에 필요한 파라미터 요청용
    model.addAttribute("pagingResponseDTO", pagingResponseDTO); // 과제 게시글 리스트가 있는 페이징
    model.addAttribute("homeworkList", pagingResponseDTO.getDtoList()); //페이징 dto안에 있는 과제 리스트

    model.addAttribute("order",order); // 강사, 학생이 속해있는 과정명 select
    model.addAttribute("sortBy",sortBy); // 강사, 학생이 속해있는 과정명 select
    //model.addAttribute("menuForAdminName",menuForAdminName); //관리자용 과정명(boolean)에 따라 출력


    return "homework/homeworkList";
  }

  @GetMapping("/homeworkSubmissionList")
  @ResponseBody
  public PagingResponseDTO<HomeworkSubmissionDTO> getHomeworkSubmissionList(
      @RequestParam(required = false) Integer submissionPageNo,
      @RequestParam(required = false) Integer submissionPageSize,
      @RequestParam(required = false) Integer homeworkId) {

    //null 체크
    if (homeworkId == null) {
      return null;
    }

    if (submissionPageNo == null) {
      submissionPageNo = 1;
    } else if (submissionPageSize == null) {
      submissionPageSize = 5;
    }


    PagingResponseDTO<HomeworkSubmissionDTO> pagingResponseSubmission =
        homeworkService.pagingSubmissionDTO((int) homeworkId,
            PagingRequestDTO.builder().pageNo((int) submissionPageNo).pageSize((int) submissionPageSize).build());
    log.info("pageNo:{}", submissionPageNo);
    log.info( "pageSize:{}",submissionPageSize);

    if (pagingResponseSubmission != null) {
      return pagingResponseSubmission;
    }
    return null;
  }

  @GetMapping("/homeworkForAdminBoolean")
  @ResponseBody
  public List<String> homeworkForAdminBoolean(@RequestParam(required = false) String progress, Model model) {

    // 관리자용 (진행 상황에 따라 과정명 select) -> select 박스 출력 성공시, 로그인 타입에 따라 컨트롤러의 서비스 메서드 구분
    log.info("progress:{}", progress);
    Boolean isInProgressForAdmin = null;

    if(progress != null && progress.contains("true")){
      isInProgressForAdmin = true;

    } else if (progress != null && progress.contains("false")) {
      isInProgressForAdmin = false;
    }
    log.info("isInProgress:{}", isInProgressForAdmin);

    List<String> menuForAdminName = homeworkService.selectBoxCourseNameForAdmin(isInProgressForAdmin);     //시험 삼아 일단 false만 출력-> 추후 select 값에 따라 출력


    if(menuForAdminName == null || menuForAdminName.isEmpty()){
      menuForAdminName = new ArrayList<>();
      menuForAdminName.add("선택된 강의가 없습니다.");
    }
    log.info("menuForAdminName:{}", menuForAdminName); //여기서 메뉴 select는 출력

    model.addAttribute("isInProgressForAdmin",isInProgressForAdmin);

    return menuForAdminName;
  }





  @GetMapping("/homeworkDetail")
  public String homeworkDetail(@RequestParam(required = false) Integer homeworkId, Model model, HttpSession session) {

    UserVO user = (UserVO)session.getAttribute("loginUser");

    if(user == null){
      return "redirect:/";
    }

    if (homeworkId == null) {
      return "redirect:/homework/homeworkList";
    }

    // homework 객체 전달
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(homeworkId);

    String instructorLoginId = "";
    if(homeworkDTO != null){
      model.addAttribute("homework",homeworkDTO);
      // 작성자 loginId 전달
      instructorLoginId = homeworkService.selectLoginId(homeworkDTO.getInstructorId());
      if(instructorLoginId != null){
        model.addAttribute("instructorLoginId",instructorLoginId);
      }
    }

    // userVO 전달
    model.addAttribute("loginUser",user);
//    model.addAttribute("homeworkId",homeworkId);

    // 파일이 있을 경우, 파일 조회
    List<FileSelectDTO> fileDTOList = utilMapper.selectFileFrom("homework",homeworkId);
    if(fileDTOList != null){
      log.info("fileDTOList:{}",fileDTOList);
      model.addAttribute("fileDTOList",fileDTOList);
    }

    // 조회수 처리
    ReadCountLog readCountLog = ReadCountLog.builder()
        .tableName("homework")
        .tableId(homeworkId)
        .userId(user.getId())
        .build();

    boolean result = homeworkService.updateReadCount(readCountLog);

    if(!result){
      log.info("조회수 증가에 오류가 있습니다.");
    } else {
      log.info("조회수 업데이트에 성공했습니다.");
    }



    return "homework/homeworkDetail";
  }

  @GetMapping("/submissionDetail")
  public String homeworkSubmissionDetail(@RequestParam(required = false) Integer submissionId,Model model,HttpSession session, HttpServletRequest request){
    // 해당 submissionid의 submission객체와 그것을 fk로 갖는 eval 객체 보내기

    String referer = request.getHeader("Referer");// 전 페이지

    String learnerIdForSubmission =homeworkService.selectUserIdForSubmission(submissionId);

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if(loginUser == null){
      return "redirect:/" + (referer != null ? referer : "homework/homeworkList");
    }

    if(learnerIdForSubmission != null){
      if(loginUser.getLoginId().equals(learnerIdForSubmission) || loginUser.getType().equals("ADMINISTRATOR") || loginUser.getType().equals("INSTRUCTOR")){


    Map<HomeworkSubmissionDTO,HomeworkEvalDTO> resultMap =homeworkService.selectSubmissionEval(submissionId);

    if(resultMap != null ){
      if(resultMap.size() > 0){

      model.addAttribute("resultMap",resultMap);
      }
    }

    log.info("모델 바인딩된 resultMap:{}",resultMap);

      return "homework/homeworkModify"; //추후 HomeworkSubmissionDetail로 바꿔주기



      }
    }



    return "redirect:/" + (referer != null ? referer : "homework/homeworkList");
  }




  @GetMapping("/submissionDetailAuth")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> homeworkDetailForLeaner(Model model, @RequestParam(required = false) Integer homeworkId, HttpSession session,
                                                                       @RequestParam(required = false) Integer submissionId) {
    //detail로 진입 전 로그인 유저 확인(타 교육생의 제출 상세 페이지 접근 막기)
    // alert 창 띄우는 용도

    //로그인 여부 확인
    if (session.getAttribute("loginUser") == null) {
      return ResponseEntity.badRequest().body(null);
    }

    //로그인한 사용자 타입 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    log.info("loginUser:{}", loginUser);

     String learnerIdForSubmission =homeworkService.selectUserIdForSubmission(submissionId);

     if(learnerIdForSubmission != null){
           if(loginUser.getLoginId().equals(learnerIdForSubmission) || loginUser.getType().equals("ADMINISTRATOR") || loginUser.getType().equals("INSTRUCTOR")){
             return ResponseEntity.ok().body(new MyResponseWithDataPYJ(200,"이동 ok","homework/submissionDetail?submissionId=" + submissionId));
           }
     }




    return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404,"해당 과제물에 접근할 수 없습니다.","/homeworkList"));
  }

  @GetMapping("/homeworkRegister")
  public String homeworkRegister(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

    // register 페이지에 접근

    //loginId 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if(loginUser == null ){
      return "redirect:/";
    } else if(!loginUser.getType().equals("INSTRUCTOR")){
      return "redirect:/homework/homeworkList";
    }


    // 해당 로그인한 강사의 현재 진행 중인 과정id와 instructorId를 select
    Map<String,Integer> resultMap = homeworkService.selectIdCourse(loginUser.getLoginId());
    if (resultMap == null){
      // 현재 진행 중인 과정이 없는 강사의 경우
      Boolean progressRegister = false;
      redirectAttributes.addFlashAttribute("progressRegister",progressRegister);

      return "redirect:/homework/homeworkList";
    }

    model.addAttribute("resultMap",resultMap);
    model.addAttribute("courseId",resultMap.get("course_id"));
    model.addAttribute("instructorId",resultMap.get("instructor_id"));

//    log.info("resultMap:{}",resultMap);
    log.info("instructor_id:{}",resultMap.get("instructor_id"));
    log.info("courseId:{}",resultMap.get("course_id"));


    return "homework/homeworkRegister";
  }

  @PostMapping("/homeworkRegister")
  public ResponseEntity<MyResponseWithDataPYJ> insertHomework(@Valid @ModelAttribute HomeworkDTO homeworkDTO,
                                                              BindingResult bindingResult, @RequestParam(required = false)MultipartFile[] files,
                                                              Model model) throws IOException {


    log.info("등록한 homeworkDTO:{}",homeworkDTO);


    if(bindingResult.hasErrors()){
      Map<String,String> errorMap = new HashMap<>();

      for(FieldError filedError:bindingResult.getFieldErrors()){
        errorMap.put(filedError.getField(),filedError.getDefaultMessage());
      }

      //model.addAttribute("errorResultMap",errorMap);
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400,"필드 에러 발생",errorMap));
    }


    // 게시글 기본 정보 등록(등록시 파일 받아오는 것까지 확인) + 등록한 해당 글의 게시글 id 반환
    int homeworkIdForFile = homeworkService.insertHomework(homeworkDTO);

    if(homeworkIdForFile != -1){
      log.info("과제 등록 성공:{}",homeworkDTO);
    }else {
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400,"과제 등록 실패",null));
    }


    if(files != null && files.length > 0){
    //일단 파일 전달 확인
      log.info("requestParam files:{}", Arrays.stream(files).toList());

      for (MultipartFile file : files) {
        // 첨부 파일 서버에 저장 + 경로 저장
        // putObject 뒤에 경로 반환
        String insertPath = s3Uploader.uploadFile("upload/homework",file.getInputStream(),file.getOriginalFilename());

      log.info("파일 서버 저장 성공");

        // 받은 파일 dto에 세팅
        // db 에서 해당 테이블의 게시글 id 다시 받아오기
        FileDTO fileDTO = FileDTO.builder()
                .originalName(file.getOriginalFilename())
                .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
                .path(insertPath)
                .size((int)file.getSize())
                .tableName("homework")
                .tableId(homeworkIdForFile)
                .build();

        // 첨부 파일 db에 저장
        int fileInsert = utilService.insertService(fileDTO);
        if(fileInsert == 1){
          log.info("파일 db에 저장 성공:{}", fileDTO);
        };
      }

    }else {
      log.info("파일 전달 안됨.");
      return ResponseEntity.ok(new MyResponseWithDataPYJ(200,"파일 등록 없는 insert 성공", homeworkDTO));
    }



    return ResponseEntity.ok(new MyResponseWithDataPYJ(200,"파일 등록 with insert 성공", homeworkDTO));

  }



  @GetMapping("/homeworkModify")
  public String homeworkModify(Model model,@RequestParam(required = false)Integer homeworkId,HttpSession session) {

    //해당 게시글의 기존 파일 리스트 조회
    List<FileSelectDTO> beforeFileList = utilService.selectFileList("homework",homeworkId);

    if(beforeFileList != null){
      if(beforeFileList.size() > 0){
        model.addAttribute("beforeFileList",beforeFileList);
      }
    }

    //로그인 유저 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if(loginUser == null ){
      return "redirect:/";
    } else if (!loginUser.getType().equals("INSTRUCTOR")) {
      return "redirect:/homework/homeworkList";
    }

    // 해당 과제의 기존 homeworkDTO 불러오기
    //해당 과제의 작성자 id가 로그인한 강사 id와 일치하는지 확인.
    int selectIsInstructor = homeworkService.selectIsInstructorId(loginUser.getLoginId(),homeworkId); //그냥 login한 아이디가 아닌, id를 바로 가져오는 것을 권장...

    if(selectIsInstructor == 0){
      // 해당 과제의 작성자가 아닌 다른 강사일 경우
      return "redirect:/homework/homeworkList";
    }

    // 해당 homeworkDTO 가져와서 수정해주기
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(homeworkId);

    if(homeworkDTO != null){
      model.addAttribute("homeworkDTO",homeworkDTO);
    }
    model.addAttribute("homeworkId",homeworkId);
  
    return "homework/homeworkModify";
  }

  @PostMapping("/homeworkModify")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> homeworkModifypost(@Valid @ModelAttribute HomeworkModifyDTO homeworkModifyDTO,
                                                              BindingResult bindingResult, @RequestParam(value = "files", required = false)List<MultipartFile> files,
                                                              @RequestParam(required = false) List<Integer> deleteFiles,
                                                              @RequestParam(required = false) Integer homeworkId,
                                                              Model model) throws IOException {

    log.info("전송 받은 homeworkModifyDTO",homeworkModifyDTO);

    if(files != null){
    log.info("전송 받은 files:{}",files);
    }

    if(deleteFiles != null){
    log.info("전송 받은 deleteFiles:{}",deleteFiles);
    }

    // 필드 에러 있을 경우
    if(bindingResult.hasErrors()){
      Map<String,String> errorMap = new HashMap<>();

      for(FieldError filedError:bindingResult.getFieldErrors()){
        errorMap.put(filedError.getField(),filedError.getDefaultMessage());
      }
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400,"필드 에러 발생",errorMap));
    }

    log.info("전송 성공한 homeworkModifyDTO:{}",homeworkModifyDTO);
    log.info("전송 성공한 files:{}",files);
    log.info("전송 성공한 deleteFiles:{}",deleteFiles);
    log.info("homeworkId:{}",homeworkId);

    // 게시글 수정
    homeworkModifyDTO.setId(homeworkId);
    int result = homeworkService.updateHomework(homeworkModifyDTO);

    if(result != 1){
      return ResponseEntity.ok(new MyResponseWithDataPYJ(200,"전송 실패",homeworkModifyDTO));
    }

    // deleteList의 id에 해당하는 파일 dto 가져오기
    List<FileSelectDTO> deleteFileList = new ArrayList<>();
    FileSelectDTO fileSelectDTO = null;

    if (deleteFiles != null) {
      if(!deleteFiles.isEmpty()){

      for (Integer num:deleteFiles) {
        fileSelectDTO = utilMapper.selectFileById(num);
        deleteFileList.add(fileSelectDTO);
      }

      log.info("삭제할 deleteFileList:{}",deleteFileList);

      // 해당 파일 dto의 getPath() 또는 지정한 dir와 getName()으로 파일 삭제해보기
      for (FileSelectDTO selectDTO : deleteFileList) {
           log.info("selectDTO.getPath:{}",selectDTO.getPath());
           s3Uploader.deleteFile("upload/homework/" + selectDTO.getNewName());
            log.info("파일 서버 삭제 성공?"); // 성공 못함

           if(utilService.deleteFileById(selectDTO.getId()) == 1){
               log.info("파일 db 삭제 성공"); // 성공
           }
      }
      }
    }

    // 수정시 생성된 파일
    if(files != null ){
      if(files.size() > 0){
        for (MultipartFile file : files) {
          // 첨부 파일 서버에 저장 + 경로 저장
          // putObject 뒤에 경로 반환
          String insertPath = s3Uploader.uploadFile("upload/homework",file.getInputStream(),file.getOriginalFilename());
          log.info("파일 서버 저장 성공");

          // 받은 파일 dto에 세팅
          // db 에서 해당 테이블의 게시글 id 다시 받아오기
          FileDTO fileDTO = FileDTO.builder()
              .originalName(file.getOriginalFilename())
              .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
              .path(insertPath)
              .size((int)file.getSize())
              .tableName("homework")
              .tableId(homeworkId)
              .build();

          // 첨부 파일 db에 저장
          int fileInsert = utilService.insertService(fileDTO);
          if(fileInsert == 1){
            log.info("파일 db에 저장 성공:{}", fileDTO);
          };
        }
      }
    }



    return ResponseEntity.ok(new MyResponseWithDataPYJ(200,"전송 성공",homeworkModifyDTO));

  }


  @DeleteMapping("/deleteHomework")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> deleteHomework(@RequestParam(required = false) Integer homeworkId) {
    log.info("deleteHomework homeworkId:{}",homeworkId);

    if(homeworkId == null){
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400,"데이터 전송 실패",null));
    }

    // 파일 불러오기
    List<FileSelectDTO> fileList = utilService.selectFileList("homework",homeworkId);

    // 파일 삭제
    if(fileList != null){
      if(fileList.size() > 0){
        for (FileSelectDTO selectDTO : fileList) {

          // 해당 파일 dto의 getPath() 또는 지정한 dir와 getName()으로 파일 삭제해보기
            s3Uploader.deleteFile("upload/homework/" + selectDTO.getNewName());
            log.info("파일 서버 삭제 성공?"); // 성공 못함

            if(utilService.deleteFileById(selectDTO.getId()) == 1){
              log.info("파일 db 삭제 성공"); // 성공
            }
        }
      }
    }

    // 게시글 삭제
    int deleteNum = homeworkService.deleteHomeworkById(homeworkId);

    if(deleteNum != 1){
    return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400,"게시글 삭제 실패",homeworkId));
    } else {
      return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "데이터 전송 성공", homeworkId));
    }
  }

}
