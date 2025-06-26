package com.goott5.lms.communityInquiry.service;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestDTO;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import com.goott5.lms.communityInquiry.mapper.InquiryMapper;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryServiceImpl implements InquiryService {

  private final InquiryMapper inquiryMapper;
  private final UserService userService;

  @Override
  public InquiryListResponse getInquiryList(InquiryRequestParam inquiryRequestParam) {

    List<InquiryVO> inquiryList = inquiryMapper.selectInquiryList(inquiryRequestParam);

    for (InquiryVO inquiryVO : inquiryList) {

      UserVO answerer = userService.findUserByUserId(inquiryVO.getAnswerer());
      String answererName = "";
      if (answerer == null) {
        answererName = "-";
      } else {
        answererName = answerer.getFullName();
      }

      inquiryVO.setAnswererName(answererName);
    }

    int boardTotalCount = inquiryMapper.selectCountBoard(inquiryRequestParam);

    int lastPage = (int) Math.ceil(boardTotalCount / (double) inquiryRequestParam.getPageSize());
    int startPage = ((int) Math.ceil(inquiryRequestParam.getPageNo() / 10.0) - 1) * 10 + 1;
    int endPage = Math.min(startPage + 9, lastPage);

    boolean prev = startPage > 1;
    boolean next = endPage < lastPage;

    InquiryListResponse inquiryListResponse = InquiryListResponse.builder()
            .inquiryList(inquiryList)
            .boardTotalCount(boardTotalCount)
            .lastPage(lastPage)
            .startPage(startPage)
            .endPage(endPage)
            .prev(prev)
            .next(next)
            .build();

    return inquiryListResponse;

  }

  @Override
  public InquiryVO getInquiryDetail(InquiryRequestParam inquiryRequestParam) {

    InquiryVO inquiryVO = inquiryMapper.selectInquiryDetail(inquiryRequestParam.getId());

    if (inquiryVO == null) {
      return null;
    } else {
      UserVO writer = userService.findUserByUserId(inquiryVO.getWriter());
      UserVO answerer = userService.findUserByUserId(inquiryVO.getAnswerer());
      String answererName = "";
      if (answerer == null) {
        answererName = "-";
      } else {
        answererName = answerer.getFullName();
      }
      inquiryVO.setWriterName(writer.getFullName());
      inquiryVO.setAnswererName(answererName);

      // 조회여부 체크
      if (inquiryRequestParam.getUserId() == inquiryVO.getWriter() && inquiryVO.isAnswered()
              && !inquiryVO.isAnsweredChecked()) {
        inquiryVO.setAnsweredChecked(true);
        inquiryMapper.updateInquiryForAnsweredChecked(inquiryRequestParam.getId());
      }

      return inquiryVO;
    }
  }

  @Override
  public int saveInquiry(InquiryRequestDTO inquiryRequestDTO) {
    return inquiryMapper.insertInquiry(inquiryRequestDTO);
  }

  @Override
  public int updateInquiry(@Valid InquiryRequestDTO inquiryRequestDTO) {
    return inquiryMapper.updateInquiry(inquiryRequestDTO);
  }

  @Override
  public int deleteInquiry(int id) {
    return inquiryMapper.updateInquiryForDelete(id);
  }

  @Override
  public int updateInquiryForAnswer(int id, String answer, int answerer) {
    return inquiryMapper.updateInquiryForAnswer(id, answer, answerer);
  }

  @Override
  public int updateInquiryForAnswerDelete(int id) {
    return inquiryMapper.updateInquiryForAnswerDelete(id);
  }
}


