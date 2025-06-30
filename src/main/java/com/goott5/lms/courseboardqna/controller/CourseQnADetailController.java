package com.goott5.lms.courseboardqna.controller;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboardqna.domain.detail.QnADetailVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.courseboardqna.service.detail.QnADetailService;
import com.goott5.lms.test.domain.Message;
import com.goott5.lms.test.domain.apiresponse.ApiResult;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CourseQnADetailController {

  private final QnADetailService qnaDetailService;
  private final SimpMessagingTemplate messagingTemplate;
  private final UtilMapper utilMapper;

  @GetMapping("/qna/{boardNo}")
  public ResponseEntity<ApiResult<QnADetailVO>> getQnADetail(@PathVariable int boardNo,
      HttpSession session) {

    QnADetailVO qnADetailVO = qnaDetailService.getQnADetail(boardNo);

    List<FileSelectDTO> fileDTOList = utilMapper.selectFileFrom("qna", boardNo);
    if (fileDTOList != null) {
      log.info("fileDTOList:{}", fileDTOList);
      qnADetailVO.setUploadFiles(fileDTOList);
    }

    return ApiResult.respondOk(200, ((UserVO) session.getAttribute("loginUser")).getType(),
        qnADetailVO
    );
  }

  @PutMapping(value = "/qna/{boardNo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public <T> ResponseEntity<ApiResult<T>> updateQnADetail(
      @Valid @ModelAttribute QnARegisterDTO qnaRegisterDTO, BindingResult bindingResult,
      @PathVariable int boardNo) throws IOException {

    // 유효성 검사
    if (bindingResult.hasErrors()) {
      Map<String, String> errorsMap = new HashMap<>();

      for (FieldError error : bindingResult.getFieldErrors()) {
        errorsMap.put(error.getField(), error.getDefaultMessage());
      }
      return ApiResult.<T>respondFail(400, "ERROR", (T) errorsMap, HttpStatus.BAD_REQUEST);
    }

    return ApiResult.respondOk(200, "SUCCESS",
        (T) qnaDetailService.updateQnADetail(boardNo, qnaRegisterDTO));
  }

  @PutMapping("/qna/{boardNo}/deleted")
  public ResponseEntity<ApiResult<String>> deleteQnADetail(@PathVariable int boardNo)
      throws UnsupportedEncodingException {

    qnaDetailService.deleteQnADetail(boardNo);
    return ApiResult.respondOk(200, "SUCCESS", "SUCCESS");
  }


  @PutMapping(value = "/qna/comment/{boardNo}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResult<String>> createQnAComment(@PathVariable int boardNo,
      @RequestBody Map<String, String> commentMap) {

    String comment = commentMap.get("comment");
    qnaDetailService.createQnAComment(comment, boardNo);

    Message isCommented = Message.builder()
        .type("commented")
        .testId(boardNo)
        .build();
    messagingTemplate.convertAndSend("/topic/qna/" + boardNo, isCommented);
    return ApiResult.respondOk(200, "SUCCESS", "SUCCESS");
  }


  @PutMapping(value = "/qna/comment2/{boardNo}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiResult<String>> updateQnAComment(@PathVariable int boardNo,
      @RequestBody Map<String, String> commentMap) {

    String comment = commentMap.get("comment");
    qnaDetailService.updateQnAComment(comment, boardNo);

    Message isCommented = Message.builder()
        .type("commented")
        .testId(boardNo)
        .build();
    messagingTemplate.convertAndSend("/topic/qna/" + boardNo, isCommented);

    return ApiResult.respondOk(200, "SUCCESS", "SUCCESS");
  }

  @PutMapping("/qna/comment/{boardNo}/deleted")
  public ResponseEntity<ApiResult<String>> deleteQnAComment(@PathVariable int boardNo) {

    qnaDetailService.deleteQnAComment(boardNo);
    return ApiResult.respondOk(200, "SUCCESS", "SUCCESS");
  }


}

