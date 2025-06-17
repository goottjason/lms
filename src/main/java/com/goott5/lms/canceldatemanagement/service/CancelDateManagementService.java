package com.goott5.lms.canceldatemanagement.service;


import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.HolidayDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface CancelDateManagementService {

  public void saveHolidays(List<HolidayDTO> holidays);

  PagingResponseDTO<CancelDateVO> getCancelDates(PagingRequestDTO pagingRequestDTO);

  void removeCancelDate(Integer id);

  void saveCancelDates(List<CancelDateDTO> cancelDateDTOS);

  List<CourseVO> getCoursesByInProgress(Integer inProgressType);

  List<CourseVO> getCoursesInProgressByDate(LocalDate cancelDate);

  List<CancelDateVO> getCancelDatesByIsAll();
}
