package com.goott5.lms.communityInquiry.service;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import com.goott5.lms.communityInquiry.mapper.InquiryMapper;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.service.UserService;
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

    List<InquiryVO> boardList = inquiryMapper.selectInquiryList(inquiryRequestParam);

    for (InquiryVO inquiryVO : boardList) {

//      UserVO writer = userService.findUserByUserId(inquiryVO.getWriter());
//      String writerName = writer.getFullName();

      UserVO answerer = userService.findUserByUserId(inquiryVO.getAnswerer());
      String answererName = "";
      if (answerer == null) {
        answererName = "-";
      } else {
        answererName = answerer.getFullName();
      }

//      inquiryVO.setWriterName(writerName);
      inquiryVO.setAnswererName(answererName);
    }

    int boardTotalCount =
            (inquiryRequestParam.getSearchType() == null || inquiryRequestParam.getSearchType()
                    .isEmpty()) ?
                    inquiryMapper.selectCountBoard()
                    : inquiryMapper.selectCountBoardWithSearchCondition(inquiryRequestParam);

    int lastPage = (int) Math.ceil(boardTotalCount / (double) inquiryRequestParam.getPageSize());
    int startPage = ((int) Math.ceil(inquiryRequestParam.getPageNo() / 10.0) - 1) * 10 + 1;
    int endPage = Math.min(startPage + 9, lastPage);

    boolean prev = startPage > 1;
    boolean next = endPage < lastPage;

    InquiryListResponse inquiryListResponse = InquiryListResponse.builder()
            .inquiryList(boardList)
            .boardTotalCount(boardTotalCount)
            .lastPage(lastPage)
            .startPage(startPage)
            .endPage(endPage)
            .prev(prev)
            .next(next)
            .build();

    return inquiryListResponse;

  }
}
