package com.goott5.lms.canceldatemanagement.service;

import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.HolidayDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingResponseDTO;
import com.goott5.lms.canceldatemanagement.mapper.CancelDateManagementMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CancelDateManagementServiceImpl implements CancelDateManagementService {

  private final CancelDateManagementMapper cancelDateManagementMapper;

  @Override
  public void saveHolidays(List<HolidayDTO> holidays) {

    // 기존 휴강일들 받아오기
    List<CancelDateDTO> oldCancelDateDTOS = cancelDateManagementMapper.selectNowAndNextYearHolidays();

    for (HolidayDTO holiday : holidays) {
      CancelDateDTO cancelDateDTO = CancelDateDTO.builder()
              .isAll(true)
              .isPublicHoliday(true)
              .cancelDate(LocalDate.parse(holiday.getLocdate(), DateTimeFormatter.BASIC_ISO_DATE))
              .reason(holiday.getDateName())
              .build();

      boolean isDuplicate = false;

      for (CancelDateDTO oldCancelDateDTO : oldCancelDateDTOS) {

        if (oldCancelDateDTO.getCancelDate().equals(cancelDateDTO.getCancelDate())
                && oldCancelDateDTO.getReason().equals(cancelDateDTO.getReason())) {
          isDuplicate = true;
          break;
        }

      }

      if (!isDuplicate) {
        cancelDateManagementMapper.insertHoliday(cancelDateDTO);
      }

    }

  }

  @Override
  public PagingResponseDTO<CancelDateVO> getCancelDates(PagingRequestDTO pagingRequestDTO) {

    List<CancelDateVO> cancelDateVOS = cancelDateManagementMapper.selectCancelDates(
            pagingRequestDTO);

    int total = cancelDateManagementMapper.selectCountOfCancelDate(pagingRequestDTO);

    log.info("cancelDateVOS:{}", cancelDateVOS);
    return PagingResponseDTO.<CancelDateVO>allInfo()
            .pagingRequestDTO(pagingRequestDTO)
            .voList(cancelDateVOS)
            .total(total)
            .build();
  }

  @Override
  public void removeCancelDate(Integer id) {

    cancelDateManagementMapper.updateDeletedAtOfCancelDate(id);
  }

  @Override
  public void saveCancelDate(CancelDateDTO cancelDateDTO) {

    cancelDateManagementMapper.insertCancelDate(cancelDateDTO);
  }

  @Override
  public List<CourseVO> getCoursesByInProgress(Integer inProgressType) {

    return cancelDateManagementMapper.selectCoursesByInProgress(inProgressType);
  }
}
