package com.goott5.lms.report.service;

import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingRequestDTO;
import com.goott5.lms.courseboarddebate.domain.CourseBoardDebatePagingResponseDTO;
import com.goott5.lms.report.domain.ReportVO;
import com.goott5.lms.report.mapper.ReportMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

  private final ReportMapper reportMapper;

  @Override
  public CourseBoardDebatePagingResponseDTO<ReportVO> getReportList(CourseBoardDebatePagingRequestDTO requestDTO) {
//    log.info(">>>> ReportService: getReportList 호출됨. 요청 페이지: {}", requestDTO.getPageNo()); // 1. 메소드 호출 확인용 로그

    List<ReportVO> dtoList = reportMapper.selectReportList(requestDTO);
    int total = reportMapper.selectReportTotalCount(requestDTO);

//    log.info(">>>> DB 조회 결과: {} 건, 전체 개수: {}", dtoList.size(), total); // 2. DB 조회 결과 확인용 로그

    return CourseBoardDebatePagingResponseDTO.<ReportVO>allInfo()
        .courseBoardDebatePagingRequestDTO(requestDTO)
        .dtoList(dtoList)
        .total(total)
        .build();
  }

  @Override
  @Transactional
  public void updateReportStatus(Long reportId, String status) {
    try {
//      log.info(">>>>>> DB 상태 업데이트 시도: reportId={}, status={} <<<<<<", reportId, status);
      reportMapper.updateReportStatus(reportId, status);
//      log.info(">>>>>> DB 업데이트 SQL 실행 완료. 커밋을 대기합니다... <<<<<<");
    } catch (Exception e) {
      // 만약 여기서 에러가 잡힌다면, 이 로그가 콘솔에 출력됩니다.
//      log.error("!!!!!!!! DB 업데이트 작업 중 심각한 에러 발생 !!!!!!!!!!", e);
      // 트랜잭션 롤백을 유지하기 위해 에러를 다시 던져줍니다.
      throw e;
    }
  }
}