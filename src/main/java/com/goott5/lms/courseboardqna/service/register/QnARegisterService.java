package com.goott5.lms.courseboardqna.service.register;

import com.goott5.lms.courseboardqna.domain.list.QnAListVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnARequestVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnAResponseVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface QnARegisterService {

  // 글 Insert
  void createQnA(@Valid QnARegisterDTO qnaRegisterDTO, HttpSession session);

  // 글 Select
  QnAResponseVO<QnAListVO> getQnAList(QnARequestVO qnaRequestVO);
}
