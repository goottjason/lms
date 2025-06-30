package com.goott5.lms.courseboardqna.service.register;

import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
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
  private final UtilMapper utilMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;

  @Override
  public int createQnA(QnARegisterDTO qnaRegisterDTO, HttpSession session) {

    QnARegisterVO qnaRegisterVO = QnARegisterVO.builder()
        .courseId(qnaRegisterMapper.selectCourseId(qnaRegisterDTO.getCourseName()))
        .writerId(((UserVO) session.getAttribute("loginUser")).getId())
        .title(qnaRegisterDTO.getTitle())
        .content(qnaRegisterDTO.getContent())
        .isSecret(qnaRegisterDTO.getIsSecret())
        .build();

    int idForQnA = -1;
    if (qnaRegisterMapper.insertQnAPost(qnaRegisterVO) == 1) {
      idForQnA = utilMapper.selectLastIdFromAll();
    }

    return idForQnA;
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
