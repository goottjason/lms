package com.goott5.lms.canceldatemanagement.controller;

import com.goott5.lms.canceldatemanagement.domain.CancelDateDTO;
import com.goott5.lms.canceldatemanagement.domain.CancelDateVO;
import com.goott5.lms.canceldatemanagement.domain.CourseVO;
import com.goott5.lms.canceldatemanagement.domain.HolidayDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingRequestDTO;
import com.goott5.lms.canceldatemanagement.domain.PagingResponseDTO;
import com.goott5.lms.canceldatemanagement.service.CancelDateManagementService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/courseManagement/cancelDateManagement")
public class CancelDateManagementController {

  private final CancelDateManagementService cancelDateManagementService;

  @GetMapping("")
  public String cancelDateManagement() {
    return "courseManagement/cancelDateManagement";
  }

  @PostMapping("/saveHoliday")
  @ResponseBody
  public void saveHolidays(@RequestBody List<HolidayDTO> holidayDTOS) {

    log.info("holidayDTOS: {}", holidayDTOS);

    cancelDateManagementService.saveHolidays(holidayDTOS);

  }

  @GetMapping("/getCoursesByInProgress")
  @ResponseBody
  public List<CourseVO> getCoursesByInProgress(Integer inProgressType) {

    log.info("isInProgress: {}", inProgressType);

    return cancelDateManagementService.getCoursesByInProgress(inProgressType);

  }


  @GetMapping("/getCancelDates")
  @ResponseBody
  public PagingResponseDTO<CancelDateVO> getCancelDates(PagingRequestDTO pagingRequestDTO) {

    log.info("pagingRequestDTO: {}", pagingRequestDTO);

    return cancelDateManagementService.getCancelDates(pagingRequestDTO);

  }

  @PostMapping("/removeCancelDate")
  @ResponseBody
  public String removeCancelDate(@RequestBody Integer id) {
    log.info("id: {}", id);

    cancelDateManagementService.removeCancelDate(id);
    return "success";
  }

  @PostMapping("/saveCancelDates")
  @ResponseBody
  public String saveCancelDates(@RequestBody List<CancelDateDTO> cancelDateDTOS) {

    log.info("cancelDateDTOS: {}", cancelDateDTOS);
    cancelDateManagementService.saveCancelDates(cancelDateDTOS);

    return "success";
  }

  @GetMapping("/getCoursesInProgressByDate")
  @ResponseBody
  public List<CourseVO> getCoursesInProgressByDate(LocalDate cancelDate) {

    return cancelDateManagementService.getCoursesInProgressByDate(cancelDate);
  }

  @GetMapping("/getCancelDatesByIsAll")
  @ResponseBody
  public List<CancelDateVO> getCancelDatesByIsAll() {

    return cancelDateManagementService.getCancelDatesByIsAll();
  }

  @GetMapping("/saveSuccess")
  public String saveSuccess(RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute("isSaveSuccess", true);
    return  "redirect:/courseManagement/cancelDateManagement";

  }

  @GetMapping("/deleteSuccess")
  public String deleteSuccess(RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute("isDeleteSuccess", true);
    return  "redirect:/courseManagement/cancelDateManagement";

  }

}
