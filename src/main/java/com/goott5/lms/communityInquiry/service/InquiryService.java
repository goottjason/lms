package com.goott5.lms.communityInquiry.service;

import com.goott5.lms.communityInquiry.domain.InquiryListResponse;
import com.goott5.lms.communityInquiry.domain.InquiryRequestParam;

public interface InquiryService {

  InquiryListResponse getInquiryList(InquiryRequestParam inquiryRequestParam);
}
