package com.goott5.lms.homework.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.homework.domain.HomeworkDTO;
import com.goott5.lms.homework.domain.HomeworkEvalDTO;
import com.goott5.lms.homework.domain.HomeworkSubmissionDTO;
import com.goott5.lms.homework.domain.MyResponseWithDataPYJ;
import com.goott5.lms.homework.domain.PagingRequestDTO;
import com.goott5.lms.homework.domain.PagingResponseDTO;
import com.goott5.lms.homework.mapper.HomeworkMapper;
import com.goott5.lms.homework.service.HomeworkService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Locked.Read;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/homework")
public class HomeworkSubmissionController {

  private final HomeworkService homeworkService;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;
  private final UtilMapper utilMapper;
  private final HomeworkMapper homeworkMapper;

  @Value("${cloud.aws.s3.bucketName}")
  private String bucket;
  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;
  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;
  @Value("${cloud.aws.region.static}")
  private String region;

  //submission 리스트 조회 d
  @GetMapping("/homeworkSubmissionList")
  @ResponseBody
  public Map<String, PagingResponseDTO<HomeworkSubmissionDTO>> getHomeworkSubmissionList(
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
            PagingRequestDTO.builder().pageNo((int) submissionPageNo)
                .pageSize((int) submissionPageSize).build());
    log.info("pageNo:{}", submissionPageNo);
    log.info("pageSize:{}", submissionPageSize);

    Map<String, PagingResponseDTO<HomeworkSubmissionDTO>> resultMap = new HashMap<>();
    if (pagingResponseSubmission != null) {
      resultMap.put(homeworkMapper.selectTitle(homeworkId), pagingResponseSubmission);
      return resultMap;
    }
    return null;
  }

  //submissionDetail
  @GetMapping("/submissionDetail")
  public String homeworkSubmissionDetail(@RequestParam(required = false) Integer submissionId,
      Model model, HttpSession session, HttpServletRequest request) {
    // 해당 submissionid의 submission객체와 그것을 fk로 갖는 eval 객체 보내기

    String referer = request.getHeader("Referer");// 전 페이지

    //submission 상세 확인 시, 아이디 검사
    String learnerIdForSubmission = homeworkService.selectUserIdForSubmission(
        submissionId); //이걸로 하지않고 로그인 아이디로 바로 검사 가능

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if (loginUser == null) {
      return "redirect:/" + (referer != null ? referer : "homework/homeworkList");
    }

    //submission 상세 확인 시, 아이디 검사
    if (learnerIdForSubmission != null) {
      if (loginUser.getLoginId().equals(learnerIdForSubmission) || loginUser.getType()
          .equals("ADMINISTRATOR") || loginUser.getType().equals("INSTRUCTOR")) {

        Map<HomeworkSubmissionDTO, HomeworkEvalDTO> resultMap = homeworkService.selectSubmissionEval(
            submissionId);

        if (resultMap == null) {
          return "redirect:/" + (referer != null ? referer : "homework/homeworkList");
        }

        if (!resultMap.isEmpty()) {

          for (Entry<HomeworkSubmissionDTO, HomeworkEvalDTO> entry : resultMap.entrySet()) {
            log.info("entry.getKey().getId():{}", entry.getKey().getId());

            // ReadCount 빌드(submission 조회수)
            ReadCountLog readCountLog = ReadCountLog.builder()
                .tableName("homework_submission")
                .tableId(entry.getKey().getId()) //!isEmpty면 무조건 키가 있음
                .userId(loginUser.getId())
                .build();

            boolean isReadSubmission = homeworkService.updateReadCountForSubmission(readCountLog);

            if (isReadSubmission) {
              log.info("readCountLog Update For Submission 성공:{}", readCountLog);
            } else {
              log.info("readCountLog Update For Submission 실패");
            }

            // 조회수 처리 끝난 submission 모델 바인딩?
            model.addAttribute("submission", entry.getKey());
            //파일 select 해올 수 있으면 모델 바인딩
            List<FileSelectDTO> submissionFiles = utilService.selectFileList("homework_submission",
                submissionId);

            if (submissionFiles != null && !submissionFiles.isEmpty()) {
              model.addAttribute("submissionFiles", submissionFiles);
              log.info("submissionFiles:{}", submissionFiles);
            }

            if (entry.getValue() != null) {

              // readCount 빌드
              ReadCountLog readCountLog1 = ReadCountLog.builder()
                  .tableName("homework_eval")
                  .tableId(entry.getValue().getId())
                  .userId(loginUser.getId())
                  .build();

              boolean isReadEval = homeworkService.updateReadCountForEval(readCountLog1);

              if (isReadEval) {
                log.info("readCountLog For Eval 성공:{}", readCountLog1);
              } else {
                log.info("readCountLog For Eval Fail 실패");
              }

              model.addAttribute("eval", entry.getValue());
              List<FileSelectDTO> evalFiles = utilService.selectFileList("homework_eval",
                  entry.getValue().getId());
              if (evalFiles != null && !evalFiles.isEmpty()) {
                model.addAttribute("evalFiles", evalFiles);
                log.info("evalFiles:{}", evalFiles);
              }

            }
          }

          model.addAttribute("loginUser", loginUser);
        }

        log.info("키와 값이 모델 바인딩 된 resultMap:{}", resultMap);

        //submissionId로 homeworkDTO도 모델 바인딩
        HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOBySubmissionId(submissionId);
        if (homeworkDTO != null) {
          model.addAttribute("homeworkDTO", homeworkDTO);
        }

        return "homework/submissionDetail"; //추후 HomeworkSubmissionDetail로 바꿔주기


      }
    }

    return "redirect:/" + (referer != null ? referer : "homework/homeworkList");
  }

  @GetMapping("/submissionDetailAuth")
  @ResponseBody
  public ResponseEntity<MyResponseWithDataPYJ> homeworkDetailForLeaner(Model model,
      HttpSession session,
      @RequestParam(required = false) Integer submissionId) {
    //detail로 진입 전 로그인 유저 확인(타 교육생의 제출 상세 페이지 접근 막기)
    // alert 창 띄우는 용도

    //로그인 여부 확인
    if (session.getAttribute("loginUser") == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(400, "로그인한 유저가 아닙니다..", null));
    }

    //로그인한 사용자 타입 확인
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    log.info("loginUser:{}", loginUser);

    String learnerIdForSubmission = homeworkService.selectUserIdForSubmission(submissionId);

    Map<HomeworkSubmissionDTO, HomeworkEvalDTO> checkMap = homeworkService.selectSubmissionEval(
        submissionId);

    if (checkMap == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(400, "반환된 map이 null", null));
    }

    int isYourInstructor = 0; //0으로 초기화

    for (HomeworkSubmissionDTO hs : checkMap.keySet()) {
      if (hs == null) {
        return ResponseEntity.badRequest()
            .body(new MyResponseWithDataPYJ(400, "반환된 submission이 null", null));
      } else {
        isYourInstructor = homeworkService.selectIsInstructorId(loginUser.getLoginId(),
            hs.getHomeworkId());
      }
    }

    if (learnerIdForSubmission != null) {
      if (loginUser.getLoginId().equals(learnerIdForSubmission) || loginUser.getType()
          .equals("ADMINISTRATOR") || (loginUser.getType().equals("INSTRUCTOR")
          && isYourInstructor == 1)) {
        log.info("성공:{}", checkMap);
        return ResponseEntity.ok().body(new MyResponseWithDataPYJ(200, "이동 ok",
            submissionId));
      }
    }

    return ResponseEntity.badRequest()
        .body(new MyResponseWithDataPYJ(404, "해당 과제물에 접근할 수 없습니다.", "/homeworkList"));
  }


  @GetMapping("/submissionRegister")
  public String submissionRegister(@RequestParam(required = false) Integer homeworkId,
      HttpSession session, Model model) {
    log.info("homeworkId:{}", homeworkId); // 파라미터 넘어옴.

    //로그인 및 homeworkId 검사
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null || homeworkId == null) {
      model.addAttribute("noAuth", "로그인한 유저가 아닙니다.");
      // 프론트에서 location.href, 바로 return(널포인터 방지)
      return "homework/submissionRegister";
    }

    if (!loginUser.getType().equals("LEARNER")) {
      model.addAttribute("noAuth", "교육생이 아닙니다.");
      return "homework/submissionRegister";
    }

    // 해당 과제의 과정을 듣는 학생이 아닐 경우 막기
    // 해당 학생이 해당 과제 id에 대한 제출기록이 1개 이상 있는 경우 막기
    if (!homeworkService.canSubmission(loginUser.getId(), homeworkId)) {
      model.addAttribute("noAuth", "해당 과제에 대한 접근 권한이 없거나, 이미 제출한 전적이 있습니다.");
      return "homework/submissionRegister";
    }

    // 제출 기한이 지났을 때
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(homeworkId);
    if (homeworkDTO != null) {
      if (homeworkDTO.getEndDate().isBefore(LocalDateTime.now())) {
        model.addAttribute("noAuth", "제출 기한이 지났습니다.");
        return "homework/submissionRegister";
      }
    }

    model.addAttribute("learnerId", loginUser.getId());

    return "homework/submissionRegister";
  }


  @PostMapping("/submissionRegister")
  public ResponseEntity<MyResponseWithDataPYJ> insertSubmission(
      @Valid @ModelAttribute HomeworkSubmissionDTO homeworkSubmissionDTO,
      BindingResult bindingResult, HttpSession session,
      @RequestParam(required = false) List<MultipartFile> fileList) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(400, "로그인한 유저가 아닙니다.", null));
    }

    if (homeworkSubmissionDTO != null) {
      homeworkSubmissionDTO.setLearnerId(loginUser.getId());
      log.info("homeworkSubmissionDTO:{}", homeworkSubmissionDTO);

      //content와 관련된 필드에러 추가
      String content = homeworkSubmissionDTO.getContent();
      if (content == null || content.isEmpty()) {
        bindingResult.addError(new FieldError("homeworkSubmissionDTO", "content", "내용을 입력해주세요."));
//        log.info("content 에러:{}", bindingResult.getFieldErrors().get(0).getDefaultMessage());
      } else {
        int length = content.getBytes(StandardCharsets.UTF_8).length;
        if (length < 10 || length > 1000) {
          bindingResult.addError(
              new FieldError("homeworkSubmissionDTO", "content", "10자 이상 1000자 이하로 입력해주세요."));
        }
      }

      Map<String, String> errorMap = new HashMap<>();
      if (bindingResult.hasErrors()) {
        for (FieldError error : bindingResult.getFieldErrors()) {
          errorMap.put(error.getField(), error.getDefaultMessage());
          log.info("errorMap:{}", errorMap);
        }
        return ResponseEntity.badRequest()
            .body(new MyResponseWithDataPYJ(400, "필드에러 발생", errorMap));
      }

      //게시글 저장
      // 게시글 insert뒤 전체 테이블의 insert된 게시글 번호를 바로반환
      int insertNum = homeworkService.insertHomeworkSubmission(homeworkSubmissionDTO);

      if (insertNum != -1) {

        if (fileList != null && !fileList.isEmpty()) {
          for (MultipartFile file : fileList) {
            try {
              // 일단 서버에 파일 저장
              String insertPath = s3Uploader.uploadFile("upload/homework", file.getInputStream(),
                  file.getOriginalFilename());
              log.info("파일 서버에 업로드 성공!");

              // db에 저장
              FileDTO fileDTO = FileDTO.builder()
                  .originalName(file.getOriginalFilename())
                  .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
                  .path(insertPath)
                  .size((int) file.getSize())
                  .tableName("homework_submission")
                  .tableId(insertNum)
                  .build();

              if (utilService.insertService(fileDTO) == 1) {
                log.info("파일 db 저장 성공");
              }
              ; //여기서 이미지 검사해 파일 insert
            } catch (IOException e) {
              log.info("파일 서버에 업로드 실패:{}", e.getMessage());
            }
          }
        }


      } else {
        return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "게시글 저장 실패", null));
      }

      return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "homeworkDTO 무사히 받음", insertNum));
    }

    return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "데이터 전송 실패", null));
  }

  @GetMapping("/submissionModify")
  public String submissionModify(HttpSession session, Model model,
      @RequestParam(required = false) Integer submissionId) {

    HomeworkSubmissionDTO submission = homeworkService.selectSubmission(submissionId);

    // 이전에 등록했던 파일 조회
    List<FileSelectDTO> beforeFiles = utilMapper.selectFileFrom("homework_submission",
        submissionId);

    if (beforeFiles != null && !beforeFiles.isEmpty()) {
      model.addAttribute("beforeFiles", beforeFiles);
    }

    //submission 일단 바인딩(null이어도)
    model.addAttribute("submission", submission);

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if (loginUser == null) {
      model.addAttribute("noAuth", "로그인한 유저가 아닙니다.");
      return "homework/submissionModify";
    }

    if (submission == null) {
      model.addAttribute("noAuth", "해당 과제가 없습니다.");
      return "homework/submissionModify";
    }

    if (submission.getLearnerId() != loginUser.getId()) {
      model.addAttribute("noAuth", "해당 과제의 작성자가 아닙니다.");
      return "homework/submissionModify";
    }

    //해당 제출물의 과제의 제출기한이 오늘보다 적을 때
    HomeworkDTO homeworkDTO = homeworkService.selectHomeworkDTOById(submission.getHomeworkId());

    if (homeworkDTO != null) {
      if (homeworkDTO.getEndDate().isBefore(LocalDateTime.now())) {
        model.addAttribute("noAuth", "제출 기한이 지나 수정 불가합니다.");
        return "homework/submissionModify";
      }
    }

    return "homework/submissionModify";
  }


  @PostMapping("/submissionModify")
  public ResponseEntity<MyResponseWithDataPYJ> submissionModify(
      @Valid @ModelAttribute HomeworkSubmissionDTO homeworkSubmissionDTO,
      BindingResult bindingResult, @RequestParam(required = false) Integer homeworkId,
      @RequestParam(required = false) Integer learnerId,
      @RequestParam(required = false) List<MultipartFile> fileList,
      @RequestParam(required = false) List<Integer> deleteFileList)
      throws UnsupportedEncodingException {

    //content와 관련된 필드에러 추가
    String content = homeworkSubmissionDTO.getContent();
    if (content == null || content.isEmpty()) {
      bindingResult.addError(new FieldError("homeworkSubmissionDTO", "content", "내용을 입력해주세요."));
//        log.info("content 에러:{}", bindingResult.getFieldErrors().get(0).getDefaultMessage());
    } else {
      int length = content.getBytes(StandardCharsets.UTF_8).length;
      if (length < 10 || length > 1000) {
        bindingResult.addError(
            new FieldError("homeworkSubmissionDTO", "content", "10자 이상 1000자 이하로 입력해주세요."));
      }
    }

    //필드 에러 발생 시
    Map<String, String> errorMap = new HashMap<>();
    if (bindingResult.hasErrors()) {
      for (FieldError error : bindingResult.getFieldErrors()) {
        errorMap.put(error.getField(), error.getDefaultMessage());
        log.info("errorMap:{}", errorMap);
      }
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(400, "필드에러 발생", errorMap));
    }

    if (homeworkSubmissionDTO == null) {
      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(404, "submission 못 받음", homeworkSubmissionDTO));
    }

    log.info("homeworkSubmissionDTO:{}", homeworkSubmissionDTO);
    log.info("fileList:{}", fileList);
    log.info("deleteFileList:{}", deleteFileList);

    // 수정 작업 시작
    homeworkSubmissionDTO.setUpdatedAt(LocalDateTime.now().withNano(0));
    int updateSubmission = homeworkService.updateSubmission(homeworkSubmissionDTO);
    log.info("homeworkSubmissionDTO update 날짜 수정:{}", homeworkSubmissionDTO); //여기까지 됨
    if (updateSubmission != 1) {
      log.info("insert 실패:{}", homeworkSubmissionDTO);
      log.info("updateSubmission 수:{}", updateSubmission);

      return ResponseEntity.badRequest()
          .body(new MyResponseWithDataPYJ(500, "게시글 insert 실패", homeworkSubmissionDTO));
    } else {
      // 게시글 insert 성공
      log.info("게시글 insert 성공:{}", homeworkSubmissionDTO);
    }

    // 삭제할 파일 삭제(서버+db)
    if (deleteFileList != null) {
      if (!deleteFileList.isEmpty()) {
        for (Integer i : deleteFileList) {
          // 서버에서 삭제? -> 안되면 추후 다시 진행
          s3Uploader.deleteFile(
              "upload/homework" + "/" + URLDecoder.decode(utilService.selectFileById(i).getNewName(),"UTF-8"));
          // db에서 삭제
          int deleteFileNum = utilService.deleteFileById(i);
          if (deleteFileNum == 1) {
            log.info("파일 삭제 성공 :{}", deleteFileNum);
          } else {
            return ResponseEntity.badRequest()
                .body(new MyResponseWithDataPYJ(500, "파일 db 삭제 실패", "id:" + i));
          }
        }
      }
    }

    // 추가할 파일 서버 업로드 + db 저장
    if (fileList != null) {
      if (!fileList.isEmpty()) {
        for (MultipartFile file : fileList) {
          // 서버 업로드
          String uploadPath = "";
          try {
            uploadPath = s3Uploader.uploadFile("upload/homework", file.getInputStream(),
                file.getOriginalFilename());
            if (!uploadPath.equals("")) {
              log.info("서버 업로드 성공");

              // 파일 DTO 세팅
              FileDTO fileDTO = FileDTO.builder()
                  .originalName(file.getOriginalFilename())
                  .newName(uploadPath.substring(uploadPath.lastIndexOf("/") + 1))
                  .path(uploadPath)
                  .size((int) file.getSize())
                  .tableName("homework_submission")
                  .tableId(homeworkSubmissionDTO.getId())
                  .build();

              // db에 파일 저장
              int fileDbNum = utilService.insertService(fileDTO);

              if (fileDbNum == 1) {
                log.info("파일 저장 성공:{}", fileDTO);
              } else {
                return ResponseEntity.badRequest()
                    .body(
                        new MyResponseWithDataPYJ(500, "파일 db 저장 실패", file.getOriginalFilename()));
              }
            }
          } catch (IOException e) {
            log.info("파일 업로드 실패:{}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new MyResponseWithDataPYJ(500, "파일 서버 업로드 실패", file.getOriginalFilename()));
          }
        }
      }

    }

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200, "수정 데이터 전송 성공", homeworkSubmissionDTO));
  }

  @DeleteMapping("/submissionDelete")
  public ResponseEntity<MyResponseWithDataPYJ> submissionDelete(
      @RequestParam(required = false) Integer submissionId,HttpSession session) {

//    1) 로그인한 유저인지
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser == null) {
      log.info("로그인 유저가 아닙니다.");
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(401,"로그인 유저가 아닙니다.",null));
    }

//    2) 해당 과제가 있는지
    if(submissionId == null){
      log.info("해당 과제가 없습니다.");
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404,"해당 과제가 없습니다.",null));
    }else if(homeworkService.selectSubmission(submissionId) == null){
      log.info("해당 과제 제출물이 없습니다.");
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(404,"해당 과제 제출물이 없습니다.",submissionId));
    }

//    3) 해당 과제의 작성자가 맞는지
    if(loginUser.getId() != homeworkService.selectSubmission(submissionId).getLearnerId()){
      log.info("해당 과제물의 작성자가 아닙니다.");
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(403,"해당 과제물의 작성자가 아닙니다.",null));
    }

//    4) 해당 제출물의 과제의 제출기한이 오늘보다 적은지
    if(homeworkService.selectHomeworkDTOById(homeworkService.selectSubmission(submissionId).getHomeworkId()).getEndDate().isBefore(LocalDateTime.now())){
      log.info("해당 과제의 제출기한이 지났습니다.");
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(409,"해당 과제의 제출기한이 지났습니다.",submissionId));
    }

    // 유효성 통과 시 게시글 삭제 + 파일 서버 삭제 + 파일 db 삭제(그 전엔 submissionId 제외하고 데이터 전송x)
    boolean result = homeworkService.deleteSubmissionById(submissionId);
    if (!result) {
      // int가 0이 아닐때
      return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(500,"해당 게시글이 삭제되지 않았습니다.",submissionId));
    }

    // 파일 삭제
    List<FileSelectDTO> fileList = utilService.selectFileList("homework_submission", submissionId);

    if(fileList != null ){
      if(!fileList.isEmpty()){
        for (FileSelectDTO fileSelectDTO : fileList) {
          //파일 서버에서 삭제
          try {
            // 서버 삭제
            s3Uploader.deleteFile("upload/homework" + "/" + URLDecoder.decode(fileSelectDTO.getNewName(), "UTF-8"));
            //서버 삭제 성공

            //db 삭제
            int deleteNum = utilService.deleteFileById(fileSelectDTO.getId());

            //db 삭제 실패 시
            if(deleteNum != 1){
              log.info("파일 db 삭제 실패:{}",fileSelectDTO);
              return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(500,"파일 db 삭제 실패", fileSelectDTO));
            }

          } catch (UnsupportedEncodingException e) {
           log.info("파일 삭제 실패",fileSelectDTO.getNewName());
           return ResponseEntity.badRequest().body(new MyResponseWithDataPYJ(500,"파일 서버 삭제 실패",fileSelectDTO));
          }
        }
      }
    }

    return ResponseEntity.ok(new MyResponseWithDataPYJ(200,"게시글 삭제 성공", null));
  }




}
