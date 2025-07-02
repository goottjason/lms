package com.goott5.lms.report.service;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.report.domain.ReportVO;

public interface ReportService {

  CourseBoardDebatePagingResponseDTO<ReportVO> getReportList(
      CourseBoardDebatePagingRequestDTO requestDTO);

  void updateReportStatus(Long reportId, String status);

}
