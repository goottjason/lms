package com.goott5.lms.courseboardqna.service.detail;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboardqna.domain.detail.QnADetailVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.courseboardqna.mapper.QnADetailMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnADetailServiceImpl implements QnADetailService {

  private final QnADetailMapper qnaDetailMapper;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;
  private final UtilMapper utilMapper;

  @Override
  public QnADetailVO getQnADetail(int boardNo) {
    return qnaDetailMapper.selectQnADetail(boardNo);
  }

  @Override
  public String updateQnADetail(int boardNo, QnARegisterDTO qnaRegisterDTO)
      throws IOException {

    qnaDetailMapper.updateQnADetail(boardNo, qnaRegisterDTO);

    log.info("qnaRegisterDTO:{}", qnaRegisterDTO);
    List<FileSelectDTO> deleteFileList = new ArrayList<>();
    FileSelectDTO fileSelectDTO = null;
    if (qnaRegisterDTO.getRemovedFileIds() != null && !qnaRegisterDTO.getRemovedFileIds()
        .isEmpty()) {
      for (long num : qnaRegisterDTO.getRemovedFileIds()) {
        fileSelectDTO = utilService.selectFileById((int) num);
        deleteFileList.add(fileSelectDTO);
      }

      for (FileSelectDTO selectDTO : deleteFileList) {
        log.info("selectDTO.getPath:{}", selectDTO.getPath());
        s3Uploader.deleteFile(
            "upload/qna/" + URLDecoder.decode(selectDTO.getNewName(), "UTF-8"));
        log.info("파일 서버 삭제 성공"); // 성공 못함

        if (utilService.deleteFileById(selectDTO.getId()) == 1) {
          log.info("파일 db 삭제 성공"); // 성공
        }
      }
    }

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
            .tableId(boardNo)
            .build();

        utilService.insertService(fileDTO);
      }
    }

    return "SUCCESS";
  }

  @Override
  public String createQnAComment(String comment, int boardNo) {

    qnaDetailMapper.updateComment(comment, boardNo);
    return "SUCCESS";
  }

  @Override
  public String updateQnAComment(String comment, int boardNo) {

    qnaDetailMapper.updateComment2(comment, boardNo);
    return "SUCCESS";
  }

  @Override
  public void deleteQnADetail(int boardNo) throws UnsupportedEncodingException {

    List<FileSelectDTO> fileList = utilService.selectFileList("qna", boardNo);

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

    qnaDetailMapper.deleteQnADetail(boardNo);
  }

  @Override
  public void deleteQnAComment(int boardNo) {

    qnaDetailMapper.deleteQnAComment(boardNo);
  }
}
