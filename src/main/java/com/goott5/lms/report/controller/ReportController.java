package com.goott5.lms.report.controller;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.report.domain.ReportVO;
import com.goott5.lms.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/operationsManagement")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/reports")
  public String getReportListPage(CourseBoardDebatePagingRequestDTO requestDTO, Model model) {

    CourseBoardDebatePagingResponseDTO<ReportVO> responseDTO = reportService.getReportList(requestDTO);

    model.addAttribute("reportList", responseDTO.getDtoList());
    model.addAttribute("responseDTO", responseDTO);

    return "operationsManagement/report";
  }
}
