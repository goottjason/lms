package com.goott5.lms.courseboardqna.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboardqna.domain.list.QnAListVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnARequestVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnAResponseVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.courseboardqna.service.register.QnARegisterService;
import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CourseQnARegisterController {

  private final QnARegisterService qnaRegisterService;
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


  @GetMapping("/qna")
  public ResponseEntity<ApiResult<QnAResponseVO<QnAListVO>>> getQnAList(
      @ModelAttribute QnARequestVO qnaRequestVO,
      HttpSession session
  ) {

    String userData =
        ((UserVO) session.getAttribute("loginUser")).getType() + "&"
            + ((UserVO) session.getAttribute(
            "loginUser")).getLoginId();

    return ApiResult.respondOk(200, userData,
        qnaRegisterService.getQnAList(qnaRequestVO));
  }

  @PostMapping(value = "/qna", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public <T> ResponseEntity<ApiResult<T>> createQnA(
      @Valid @ModelAttribute QnARegisterDTO qnaRegisterDTO, BindingResult bindingResult,
      HttpSession session
  ) throws IOException {

    // 유효성 검사
    if (bindingResult.hasErrors()) {
      Map<String, String> errorsMap = new HashMap<>();

      for (FieldError error : bindingResult.getFieldErrors()) {
        errorsMap.put(error.getField(), error.getDefaultMessage());
      }
      return ApiResult.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    log.info("qnaRegisterService : {}", qnaRegisterDTO);
    int qnaIdForFile = qnaRegisterService.createQnA(qnaRegisterDTO, session);

    if (qnaRegisterDTO.getUploadFiles() != null && qnaRegisterDTO.getUploadFiles().size() > 0) {
      for (MultipartFile file : qnaRegisterDTO.getUploadFiles()) {

        String insertPath = s3Uploader.uploadFile("upload/qna", file.getInputStream(),
            file.getOriginalFilename());

        log.info("파일 서버 저장 성공");

        FileDTO fileDTO = FileDTO.builder()
            .originalName(file.getOriginalFilename())
            .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
            .path(insertPath)
            .size((int) file.getSize())
            .tableName("qna")
            .tableId(qnaIdForFile)
            .build();

        utilService.insertService(fileDTO);
      }
    }

    return ApiResult.<T>respondOk(200, "SUCCESS", (T) "SUCCESS");
  }

}
