package com.goott5.lms.canceldatemanagement.service;

import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.HolidayDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingResponseDTO;
import com.goott5.lms.canceldatemanagement.domain.ScheduleDTO;
import com.goott5.lms.canceldatemanagement.mapper.CancelDateManagementMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

    List<CancelDateVO> cancelDatesAllIsTrue = cancelDateManagementMapper.selectCancelDatesByIsAll();

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

        List<CourseVO> courseVOS = cancelDateManagementMapper.selectCoursesInProgressByDate(cancelDateDTO.getCancelDate());

        for (CourseVO courseVO : courseVOS) {

          CancelDateDTO oneCancelDateDTO = CancelDateDTO.builder()
                  .isAll(false)
                  .courseId(courseVO.getId())
                  .isPublicHoliday(true)
                  .cancelDate(cancelDateDTO.getCancelDate())
                  .reason(cancelDateDTO.getReason())
                  .build();

          removeAndInsertSchedulesForSaveCancelDate(oneCancelDateDTO, cancelDatesAllIsTrue);
        }

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

    CancelDateDTO cancelDateDTO = cancelDateManagementMapper.selectCancelDateForDelete(id);

    if (cancelDateDTO.isAll()) { // 전체과정 스케쥴 딜리트 & 인서트

      List<CourseVO> courseVOS = cancelDateManagementMapper.selectCoursesInProgressByDate(
              cancelDateDTO.getCancelDate());
      for (CourseVO courseVO : courseVOS) {
        CancelDateDTO oneCancelDateDTO = CancelDateDTO.builder()
                .isAll(false)
                .courseId(courseVO.getId())
                .isPublicHoliday(false)
                .cancelDate(cancelDateDTO.getCancelDate())
                .reason(cancelDateDTO.getReason())
                .build();
        removeAndInsertSchedulesForDeleteCancelDate(oneCancelDateDTO);
      }

    } else { // 해당과정 스케쥴 딜리트 & 인서트

      removeAndInsertSchedulesForDeleteCancelDate(cancelDateDTO);
    }

    cancelDateManagementMapper.deleteCancelDate(id);

  }

  @Override
  public void saveCancelDates(List<CancelDateDTO> cancelDateDTOS) {

    List<CancelDateVO> cancelDatesAllIsTrue = cancelDateManagementMapper.selectCancelDatesByIsAll();

    // 해당 날짜에 이미 개별 휴강일이 있던 강의들 조회

    for (CancelDateDTO cancelDateDTO : cancelDateDTOS) {
      int checkii = cancelDateManagementMapper.insertCancelDate(cancelDateDTO);
      log.info("checkdd:{}", checkii);
      if (cancelDateDTO.isAll()) { // 해당 날짜에 진행중인 과정 모두 시간표 업데이트

        int checkdd = cancelDateManagementMapper.deleteAllCancelDateByDateExceptIsAllTrue(cancelDateDTO.getCancelDate());
        log.info("checkdd:{}", checkdd);

        List<CourseVO> courseVOS = cancelDateManagementMapper.selectCoursesInProgressByDate(
                cancelDateDTO.getCancelDate());

        for (CourseVO courseVO : courseVOS) {
          CancelDateDTO oneCancelDateDTO = CancelDateDTO.builder()
                  .isAll(false)
                  .courseId(courseVO.getId())
                  .isPublicHoliday(false)
                  .cancelDate(cancelDateDTO.getCancelDate())
                  .reason(cancelDateDTO.getReason())
                  .build();
          removeAndInsertSchedulesForSaveCancelDate(oneCancelDateDTO, cancelDatesAllIsTrue);
        }

      } else { // 해당 과정만 시간표 업데이트

        removeAndInsertSchedulesForSaveCancelDate(cancelDateDTO, cancelDatesAllIsTrue);
      }
    }

    // 입력한 휴강일에 따라 스케쥴러 미루기 & 코스 엔드데이트 업데이트
  }

  @Override
  public List<CourseVO> getCoursesByInProgress(Integer inProgressType) {

    return cancelDateManagementMapper.selectCoursesByInProgress(inProgressType);
  }

  @Override
  public List<CourseVO> getCoursesInProgressByDate(LocalDate cancelDate) {
    return cancelDateManagementMapper.selectCoursesInProgressByDate(cancelDate);
  }

  @Override
  public List<CancelDateVO> getCancelDatesByIsAll() {
    return cancelDateManagementMapper.selectCancelDatesByIsAll();
  }

  private void removeAndInsertSchedulesForSaveCancelDate(CancelDateDTO cancelDateDTO,
          List<CancelDateVO> cancelDatesAllIsTrue) {

    if(cancelDateManagementMapper.selectCountOfSchedulesByCourseAndDate(cancelDateDTO) < 1){
      return;
    };

    List<LocalDate> classDates = cancelDateManagementMapper.selectClassDates(cancelDateDTO);

    int startIndex = classDates.indexOf(cancelDateDTO.getCancelDate());

    // remainDates의 마지막 요소에서 하루 더한날이 주말이나 공휴일인지 확인 후 아닐 시 newDates에 add
    List<LocalDate> remainDates = new ArrayList<LocalDate>(
            classDates.subList(startIndex, classDates.size()));

    List<LocalDate> newDates = new ArrayList<LocalDate>(
            remainDates.subList(1, remainDates.size()));

    LocalDate newLastDate = remainDates.get(remainDates.size() - 1);
    do {
      newLastDate = newLastDate.plusDays(1);
      boolean isValidDay = true;
      for (CancelDateVO cancelDateVO : cancelDatesAllIsTrue) {
        if (newLastDate.equals(cancelDateVO.getCancelDate())
                || newLastDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || newLastDate.getDayOfWeek() == DayOfWeek.SUNDAY) {

          isValidDay = false;
        }
      }
      if (isValidDay) {
        newDates.add(newLastDate);
      }

    } while (remainDates.size() != newDates.size());

    Collections.reverse(remainDates);
    Collections.reverse(newDates);

    List<ScheduleDTO> scheduleDTOS = cancelDateManagementMapper.selectRemainSchedules(
            cancelDateDTO);

    Collections.reverse(scheduleDTOS);

    for (ScheduleDTO scheduleDTO : scheduleDTOS) {
      for (int i = 0; i < remainDates.size(); i++) {
        if (scheduleDTO.getClassDate().equals(remainDates.get(i))) {
          scheduleDTO.setClassDate(newDates.get(i));
          break;
        }
      }
    }

    Collections.reverse(scheduleDTOS);
    cancelDateManagementMapper.deleteRemainSchedules(cancelDateDTO);
    cancelDateManagementMapper.insertNewSchedules(scheduleDTOS);
    cancelDateManagementMapper.updateCourseEndDate(cancelDateDTO.getCourseId(), newLastDate);
  }

  private void removeAndInsertSchedulesForDeleteCancelDate(CancelDateDTO cancelDateDTO) {

    List<LocalDate> remainDates = cancelDateManagementMapper.selectRemainClassDates(
            cancelDateDTO);

    List<LocalDate> newDates = new ArrayList<>(remainDates);
    newDates.remove(remainDates.size() - 1);
    newDates.add(0, cancelDateDTO.getCancelDate());

    List<ScheduleDTO> scheduleDTOS = cancelDateManagementMapper.selectRemainSchedules(
            cancelDateDTO);

    for (ScheduleDTO scheduleDTO : scheduleDTOS) {

      for (int i = 0; i < remainDates.size(); i++) {
        if (scheduleDTO.getClassDate().equals(remainDates.get(i))) {
          scheduleDTO.setClassDate(newDates.get(i));
          break;
        }
      }
    }

    cancelDateManagementMapper.deleteRemainSchedules(cancelDateDTO);
    cancelDateManagementMapper.insertNewSchedules(scheduleDTOS);
    cancelDateManagementMapper.updateCourseEndDate(cancelDateDTO.getCourseId(), newDates.get(
            newDates.size() - 1));
  }
}
