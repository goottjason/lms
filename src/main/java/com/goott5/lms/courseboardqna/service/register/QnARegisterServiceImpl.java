package com.goott5.lms.courseboardqna.service.register;

import com.goott5.lms.courseboardqna.domain.list.QnAListVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnARequestVO;
import com.goott5.lms.courseboardqna.domain.pagination.QnAResponseVO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterDTO;
import com.goott5.lms.courseboardqna.domain.register.QnARegisterVO;
import com.goott5.lms.courseboardqna.mapper.QnARegisterMapper;
import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QnARegisterServiceImpl implements QnARegisterService {

  private final QnARegisterMapper qnaRegisterMapper;

  @Override
  public void createQnA(QnARegisterDTO qnaRegisterDTO, HttpSession session) {

    QnARegisterVO qnaRegisterVO = QnARegisterVO.builder()
        .courseId(qnaRegisterMapper.selectCourseId(qnaRegisterDTO.getCourseName()))
        .writerId(((UserVO) session.getAttribute("loginUser")).getId())
        .title(qnaRegisterDTO.getTitle())
        .content(qnaRegisterDTO.getContent())
        .isSecret(qnaRegisterDTO.getIsSecret())
        .build();

    qnaRegisterMapper.insertQnAPost(qnaRegisterVO);
  }

  @Override
  public QnAResponseVO<QnAListVO> getQnAList(QnARequestVO qnaRequestVO) {

    qnaRequestVO.setTotalItemsCount(qnaRegisterMapper.selectQnAPostCount(qnaRequestVO));

    return QnAResponseVO.<QnAListVO>allInfo()
        .qnaRequestVO(qnaRequestVO)
        .items(qnaRegisterMapper.selectQnAPost(qnaRequestVO))
        .build();
  }

}
