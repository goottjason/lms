package com.goott5.lms.courseboardqna.service.detail;

import com.goott5.lms.courseboardqna.domain.detail.QnADetailVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface QnADetailService {

  // 상세 조회
  QnADetailVO getQnADetail(int boardNo);

  // 글 수정
  String updateQnADetail(int boardNo, QnARegisterDTO qnaRegisterDTO)
      throws IOException;

  // 답변 등록
  String createQnAComment(String comment, int boardNo);

  // 답변 수정
  String updateQnAComment(String comment, int boardNo);

  // 글 삭제
  void deleteQnADetail(int boardNo) throws UnsupportedEncodingException;

  // 답변 삭제
  void deleteQnAComment(int boardNo);
}
