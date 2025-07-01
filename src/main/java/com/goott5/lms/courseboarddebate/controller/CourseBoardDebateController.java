package com.goott5.lms.courseboarddebate.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateCommentDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateDetailInfo;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePageDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebateReport;
import com.goott5.lms.courseboarddebate.domain.MyResponseWithDataDebate;
import com.goott5.lms.courseboarddebate.service.CourseBoardDebateService;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/courseBoardDebate")
public class CourseBoardDebateController {

  private final CourseBoardDebateService courseBoardDebateService;
  private final UtilMapper utilMapper;
  private final UtilService utilService;
  private final S3Uploader s3Uploader;
  private final CourseManagementService courseManagementService;

  // 리스트 조회
  @GetMapping("/debateList")
  public String getDebateList(
      @ModelAttribute("requestDTO") CourseBoardDebatePagingRequestDTO courseBoardDebatePagingRequestDTO, Model model , HttpSession session,
      @RequestParam(required = false) String courseName) {

    log.info("페이지네이션No: {}", courseBoardDebatePagingRequestDTO.getPageNo());

    if(courseName != null){
      courseBoardDebatePagingRequestDTO.setCourseName(courseName);
      log.info("courseName:{}", courseName);
    }

    // 로그인 사용자 타입 가져오기 (NullPointerException 방지)
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    Integer loginUserId = loginUser.getId();
    String loginUserType = loginUser.getType(); // 기본값: 로그인하지 않은 사용자

    if (courseBoardDebatePagingRequestDTO.getPageNo() == 0) { // int 기본값은 0
      courseBoardDebatePagingRequestDTO.setPageNo(1);
    }
    if (courseBoardDebatePagingRequestDTO.getPagingSize() == 0) {
      courseBoardDebatePagingRequestDTO.setPagingSize(10);
    }

    if (courseBoardDebatePagingRequestDTO.getCourseId() == null && !"ADMINISTRATOR".equals(loginUserType)) {

      CommonReqDTO commonReqDTO = CommonReqDTO.builder()
          .loginUserId(loginUser.getId())
          .loginUserType(loginUserType)
          .build();
      PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = PageCourseReqDTO.<CourseReqDTO>builder()
          .orderBy("name").orderDirection("ASC").build();

      PageCourseRespDTO<CourseRespDTO> courses = courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

      // 조회된 과정이 있을 경우에만 첫 번째 과정 ID를 기본값으로 설정
      if (courses != null && !courses.getRespDTOS().isEmpty()) {
        Integer defaultCourseId = courses.getRespDTOS().get(0).getId();
        courseBoardDebatePagingRequestDTO.setCourseId(defaultCourseId);
        log.info("사용자 기본 과정 ID를 설정합니다: {}", defaultCourseId);
      }
    }

    CourseBoardDebatePagingResponseDTO<CourseBoardDebatePageDTO> responseDTO = courseBoardDebateService.getCourseBoardDebateList(courseBoardDebatePagingRequestDTO,session);

    log.info("responseDTO={}", responseDTO.getDtoList());

    if (courseBoardDebatePagingRequestDTO.getKeyword() == null || courseBoardDebatePagingRequestDTO.getKeyword().isEmpty()) {
      session.setAttribute("keyword", courseBoardDebatePagingRequestDTO.getKeyword());
    }

    model.addAttribute("responseDTO", responseDTO);

    model.addAttribute("pagingRequestDTO", courseBoardDebatePagingRequestDTO);

    log.info("pageNo={}", courseBoardDebatePagingRequestDTO.getPageNo());
    log.info("pagingSize={}", courseBoardDebatePagingRequestDTO.getPagingSize());



    if (loginUser != null && loginUser.getType() != null) {
      loginUserType = loginUser.getType(); // 세션에서 가져온 사용자 타입
    }


    model.addAttribute("loginUserType", loginUserType); // 변수명을 loginUserType 으로 통일
    log.info("loginUserType={}", loginUserType);


    return "courseBoardDebate/debateList";

  }


   // 상세 페이지(GET)
  @GetMapping("/debateDetail")
  public String getDebateDetail(@RequestParam("id") Integer id, HttpSession session,
      @ModelAttribute("pagingRequestDTO") CourseBoardDebatePagingRequestDTO pagingRequestDTO,
      Model model) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    // 로그인 여부 확인
    if (loginUser == null) {
      return "redirect:/";
    }

    // 파라미터로 받은 id null 체크
    if (id == null) {
      return "courseBoardMaterials/materialsList";
    }

    log.info("상세 페이지 요청 ID : {}", id);

    CourseBoardDebateDetailInfo debateDetail = courseBoardDebateService.getCourseBoardDebateDetail(id, loginUser);

    if (debateDetail == null) {
      log.warn("ID {} 에 해당하는 상세 정보를 찾을 수 없습니다.", id);
      return "redirect:/courseBoardDebate/debateList?" + pagingRequestDTO.getLink();
    }

    // 모델에 데이터 추가
    model.addAttribute("debateDetail", debateDetail);
    model.addAttribute("user", loginUser);
    model.addAttribute("loginUserType", loginUser.getType());
    model.addAttribute("loginUserId", loginUser.getId());
    model.addAttribute("currentCourseId", debateDetail.getCourseId());
    model.addAttribute("pagingRequestDTO", pagingRequestDTO);
    model.addAttribute("fileDTOList", debateDetail.getAttachments());

    // 파일 조회 처리
    List<FileSelectDTO> fileDTOList = utilMapper.selectFileFrom("course_forum", id);
    if (fileDTOList != null) {
      log.info("fileDTOList : {}", fileDTOList);
      model.addAttribute("fileDTOList", fileDTOList);
    }

    // 조회수 증가 로직 처리
    ReadCountLog readCountLog = ReadCountLog.builder()
        .tableName("course_forum")
        .tableId(id)
        .userId(loginUser.getId()) // 처음에 가져온 loginUser 변수 사용
        .build();

    boolean result = courseBoardDebateService.updateCourseBoardDebateReadCount(readCountLog);

    if (!result) {
      log.info("조회수 증가 처리 중 오류 또는 이미 오늘 조회한 사용자.");
    } else {
      log.info("조회수가 성공적으로 업데이트 되었습니다.");
    }

    log.info("상세 정보 불러오기 성공 : {}", debateDetail);

    return "courseBoardDebate/debateDetail";
  }

  // 등록 페이지(GET)
  @GetMapping("/debateRegister")
  public String getDebateRegister(@ModelAttribute CourseBoardDebatePagingRequestDTO pagingRequestDTO, Model model) {
    CourseBoardDebateDTO debateDto = new CourseBoardDebateDTO();
    // 목록에서 선택했던 courseId를 새 글 DTO의 기본값으로 설정
    if (pagingRequestDTO.getCourseId() != null) {
      debateDto.setCourseId(pagingRequestDTO.getCourseId());
    }
    model.addAttribute("courseBoardDebateDTO", debateDto);
    // 목록의 상태 정보를 담은 DTO를 뷰로 전달
    model.addAttribute("pagingRequestDTO", pagingRequestDTO);
    model.addAttribute("currentCourseId", pagingRequestDTO.getCourseId());

    return "courseBoardDebate/debateRegister";
  }

  // 등록 처리(POST)
  @PostMapping("/debateRegister")
  public ResponseEntity<MyResponseWithDataDebate> insertMaterials(@Valid @ModelAttribute CourseBoardDebateDTO courseBoardDebateDTO
      , BindingResult bindingResult, @RequestParam(required = false) List<MultipartFile> files, HttpSession session)
      throws IOException {
    log.info("등록된 DTO={}", courseBoardDebateDTO);

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.status(401).body(new MyResponseWithDataDebate(401,"로그인이 필요합니다!",null));
    }
    courseBoardDebateDTO.setWriterId(loginUser.getId());

    String content = courseBoardDebateDTO.getContent();
    if (content == null || content.isBlank()) {
      bindingResult.addError(
          new FieldError("courseBoardDebateDTO", "content", "글자를 입력해주세요."));
    } else {
      int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
      if (contentLength > 1000 || contentLength < 20) {
        bindingResult.addError(new FieldError("courseBoardDebateDTO", "content",
            "글자 20자 이상 1000자 이하여야 합니다."));
      }
    }

    if (bindingResult.hasErrors()) {
      Map<String, String> errorMap = new HashMap<>();

      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataDebate(400, "에러 발생!!!!", errorMap));
    }
    int courseBoardMaterialsFile = courseBoardDebateService.insertCourseBoardDebate(courseBoardDebateDTO);


    if (courseBoardMaterialsFile != -1) {
      log.info("등록 성공!!!={}", courseBoardDebateDTO);
    } else {
      log.info("등록실패!!");
    }

    log.info("files={}", files);

    if (files != null && !files.isEmpty()) {

      log.info("파일 확인 ={}", files);

      for (MultipartFile file : files) {
        if (file.isEmpty()){
          log.info("파일 없다. ={}",file.isEmpty());
          continue;
        }

        // 첨부파일 서버에 저장 + 경로 저장
        String insertPath = s3Uploader.uploadFile("upload/course_forum",file.getInputStream(),
            file.getOriginalFilename());

        log.info("파일 저장 성공!!");

        // 받은 파일 dto에 세팅
        // db 에서 해당 테이블의 게시글 id 다시 받아오기
        FileDTO fileDTO = FileDTO.builder()
            .originalName(file.getOriginalFilename())
            .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
            .path(insertPath)
            .size((int) file.getSize())
            .tableName("course_forum")
            .tableId(courseBoardMaterialsFile)
            .build();

        // 첨부 파일 db에 저장
        int fileInsert = utilService.insertService(fileDTO);
        if (fileInsert == 1) {
          log.info("파일 db에 저장 성공:{}", fileDTO);
        }
        ;
      }

    } else {
      log.info("파일 전달 안됨.");
      return ResponseEntity.ok(
          new MyResponseWithDataDebate(200, "글 작성이 완료되었습니다.", courseBoardDebateDTO));

    }

    return ResponseEntity.ok(
        new MyResponseWithDataDebate(200, "글 작성이 완료되었습니다.", courseBoardDebateDTO));

  }

  // 수정 페이지(GET)
  @GetMapping("/debateModify")
  public String getDebateModify(@RequestParam(required = false) int id, Model model,HttpSession session) {

    List<FileSelectDTO> attachments = utilService.selectFileList("course_forum",id);

    if (attachments != null && attachments.size() > 0) {
      model.addAttribute("attachments", attachments);
    }

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/";
    }

    CourseBoardDebateDetailInfo detail = courseBoardDebateService.getCourseBoardDebateDetail(id, loginUser);

    CourseBoardDebateDTO dto = new CourseBoardDebateDTO();
    dto.setId(detail.getId());
    dto.setCourseId(detail.getCourseId());
    dto.setTitle(detail.getTitle());
    dto.setContent(detail.getContent());

    model.addAttribute("currentCourseId", dto.getCourseId());
    model.addAttribute("courseBoardDebate", dto);

    return "courseBoardDebate/debateModify";
  }

  // 수정 처리(POST)
  @PostMapping("/debateModify")
  public ResponseEntity<MyResponseWithDataDebate> postDebateModify(
      @Valid @ModelAttribute CourseBoardDebateDTO courseBoardDebateDTO,
      BindingResult bindingResult,
      @RequestParam(value = "files", required = false) List<MultipartFile> files, // 새로 첨부된 파일
      @RequestParam(required = false) List<Integer> deleteFiles) throws IOException {

    log.info("전송 받은 DTO = {}", courseBoardDebateDTO);

    log.info("DTO에 바인딩된 게시글 ID: {}", courseBoardDebateDTO.getId());

    if (files != null) {
      log.info("전송 받은 files: {}", files);
    }

    if (deleteFiles != null) {
      log.info("전송 받은 deleteFiles: {}", deleteFiles);
    }

    if (bindingResult.hasErrors()) {
      Map<String, String> errorMap = new HashMap<>();

      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
      return ResponseEntity.badRequest().body(new MyResponseWithDataDebate(400,"에러 발생!!", errorMap));
    }
    log.info("전송 성공 DTO ={}", courseBoardDebateDTO);
    log.info("전송 성공 files ={}", files);
    log.info("전송 성공 deleteFiles ={}", deleteFiles);

    // 게시글 수정
    int result = courseBoardDebateService.updateCourseBoardDebate(courseBoardDebateDTO);

    if (result != 1) {
      return ResponseEntity.ok(new MyResponseWithDataDebate(500,"전송 실패!!",courseBoardDebateDTO));
    }

    List<FileSelectDTO> deleteFileList = new ArrayList<>();
    FileSelectDTO fileSelectDTO = null;

    if (deleteFiles != null) {
      if (!deleteFiles.isEmpty()) {

        for (Integer num : deleteFiles) {
          fileSelectDTO = utilMapper.selectFileById(num);
          deleteFileList.add(fileSelectDTO);
        }

        log.info("삭제할 deleteFiles ={}", deleteFileList);

        for (FileSelectDTO selectDTO : deleteFileList) {
          log.info("selectDTO.getPath:{}", selectDTO.getPath());
          s3Uploader.deleteFile("upload/course_forum/" + selectDTO.getNewName());
          log.info("파일 서버 삭제 성공?"); // 성공 못함

          if (utilService.deleteFileById(selectDTO.getId()) == 1) {
            log.info("파일 db 삭제 성공"); // 성공
          }
        }
      }
    }

    // 수정시 생성된 파일
    if (files != null && !files.isEmpty()) {

      log.info(">>>>> 파일 처리 블록에 진입했습니다. 감지된 파일 개수: {}개 <<<<<", files.size());

      if (courseBoardDebateDTO.getId() == 0) {
        log.error("게시글 ID가 없어 파일을 저장할 수 없습니다. DTO: {}", courseBoardDebateDTO);
        return ResponseEntity.internalServerError()
            .body(new MyResponseWithDataDebate(500, "오류로 인해 파일 저장에 실패했습니다.", null));
      }

      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          continue;
        }
        // 첨부 파일 서버에 저장 + 경로 저장
        String insertPath = s3Uploader.uploadFile("upload/course_forum", file.getInputStream(),
            file.getOriginalFilename());
        log.info("파일 서버 저장 성공");

        // 받은 파일 dto에 세팅
        FileDTO fileDTO = FileDTO.builder()
            .originalName(file.getOriginalFilename())
            .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
            .path(insertPath)
            .size((int) file.getSize())
            .tableName("course_forum")
            .tableId(courseBoardDebateDTO.getId())
            .build();

        // 첨부 파일 db에 저장
        int fileInsert = utilService.insertService(fileDTO);
        if (fileInsert == 1) {
          log.info("파일 db에 저장 성공: {}", fileDTO);
        } else {
          log.warn("<<<<< 파일 DB 저장 실패! service의 반환값: {}, 저장하려던 정보: {} >>>>>", fileInsert, fileDTO);
        }
      }

    } else {
      // 만약 파일을 받지 못했다면 이 로그가 찍힐 것입니다.
      log.warn(">>>>> 전송된 파일이 없어 파일 처리 블록을 건너뜁니다. 'files' 파라미터가 비어있습니다. <<<<<");
    }

    return ResponseEntity.ok(new MyResponseWithDataDebate(200, "수정 완료", courseBoardDebateDTO));
  }

  // 삭제 처리
  @DeleteMapping("/{debateId}")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataDebate> deleteMaterial(@PathVariable("debateId") int debateId) {
    try {
      // 서비스 계층에 구현한 삭제 로직 호출
      courseBoardDebateService.deleteCourseBoardDebate(debateId);

      // 성공 시 JSON 형태로 응답
      return ResponseEntity.ok(new MyResponseWithDataDebate(200,"게시글이 삭제되었습니다.",debateId));

    } catch (Exception e) {
      log.error("게시글 삭제 중 오류 발생: id={}", debateId, e);
      // 실패 시 서버 에러 응답
      return ResponseEntity.internalServerError().body(new MyResponseWithDataDebate(500,"삭제 중 오류가 발생했습니다.",debateId));
    }
  }

  // 댓글 작성
  @PostMapping("/comments")
  public String addComment(@RequestParam("forumId") int forumId, @RequestParam String content, HttpSession session,
      @ModelAttribute CourseBoardDebatePagingRequestDTO pagingRequestDTO) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/login";
    }

    CourseBoardDebateCommentDTO comment = new CourseBoardDebateCommentDTO();
    comment.setCourseForumId(forumId);
    comment.setContent(content);
    comment.setCommenterId(loginUser.getId());

    String url = pagingRequestDTO.generateLinkExceptPageNo();

    courseBoardDebateService.addCourseBoardDebateComment(comment);
    return "redirect:/courseBoardDebate/debateDetail?id=" + forumId + "&pageNo=" + pagingRequestDTO.getPageNo() + url;
  }

  // 댓글 수정
  @PutMapping("/comments")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataDebate> updateComment(
      @RequestBody Map<String, String> payload,
      HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.status(401).body(new MyResponseWithDataDebate(401, "로그인이 필요합니다.", null));
    }

    try {
      int commentId = Integer.parseInt(payload.get("commentId"));
      String content = payload.get("content");

      courseBoardDebateService.updateComment(commentId, content, loginUser);

      return ResponseEntity.ok(new MyResponseWithDataDebate(200, "댓글이 수정되었습니다.", null));

    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest().body(new MyResponseWithDataDebate(400, "잘못된 댓글 ID입니다.", null));
    } catch (IllegalArgumentException e) {
      // 서비스에서 권한 없음 등의 이유로 발생시킨 예외 처리
      return ResponseEntity.status(403).body(new MyResponseWithDataDebate(403, e.getMessage(), null));
    }
  }

  // 댓글 삭제
  @DeleteMapping("/comments/{commentId}")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataDebate> deleteComment(
      @PathVariable int commentId,
      HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.status(401).body(new MyResponseWithDataDebate(401, "로그인이 필요합니다.", null));
    }

    try {
      courseBoardDebateService.deleteComment(commentId, loginUser);
      return ResponseEntity.ok(new MyResponseWithDataDebate(200, "댓글이 삭제되었습니다.", null));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(403).body(new MyResponseWithDataDebate(403, e.getMessage(), null));
    }
  }

  // 좋아요
  @PostMapping("/like")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataDebate> toggleLike(@RequestParam("id") int id, HttpSession session) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.status(401).body(new MyResponseWithDataDebate(401, "로그인이 필요합니다.", null));
    }
    int finalLikeCount = courseBoardDebateService.toggleCourseBoardDebateLike(id, loginUser.getId());
    return ResponseEntity.ok(new MyResponseWithDataDebate(200, "처리 완료", Map.of("likeCount", finalLikeCount)));
  }

  // 신고 접수
  @PostMapping("/report")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataDebate> addReport(@RequestParam int id, @RequestParam String reportDetail, HttpSession session) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.status(401).body(new MyResponseWithDataDebate(401, "로그인이 필요합니다.", null));
    }

    CourseBoardDebateReport report = new CourseBoardDebateReport();
    report.setCourseForumId(id);
    report.setUserId(loginUser.getId());
    report.setReportDetail(reportDetail);

    courseBoardDebateService.addCourseBoardDebateReport(report);
    return ResponseEntity.ok(new MyResponseWithDataDebate(200, "신고가 정상적으로 접수되었습니다.", null));
  }
}