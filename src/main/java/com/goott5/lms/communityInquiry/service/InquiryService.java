package com.goott5.lms.communityInquiry.service;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestDTO;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;
import com.goott5.lms.communityInquiry.domain.InquiryVO;
import jakarta.validation.Valid;

public interface InquiryService {

  InquiryListResponse getInquiryList(InquiryRequestParam inquiryRequestParam);

  InquiryVO getInquiryDetail(InquiryRequestParam inquiryRequestParam);

  int saveInquiry(@Valid InquiryRequestDTO inquiryRequestDTO);

  int updateInquiry(InquiryRequestDTO inquiryRequestDTO);

  int deleteInquiry(int id);

  int updateInquiryForAnswer(int id, String answer, int answerer);

  int updateInquiryForAnswerDelete(int id);
}
