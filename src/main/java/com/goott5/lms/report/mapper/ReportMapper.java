package com.goott5.lms.report.mapper;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.report.domain.ReportVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper {

  List<ReportVO> selectReportList(CourseBoardDebatePagingRequestDTO requestDTO);
  int selectReportTotalCount(CourseBoardDebatePagingRequestDTO requestDTO);
  void updateReportStatus(@Param("reportId") Long reportId, @Param("status") String status);

}
