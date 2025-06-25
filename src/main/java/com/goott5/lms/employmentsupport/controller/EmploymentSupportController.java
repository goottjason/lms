package com.goott5.lms.employmentsupport.controller;

import com.goott5.lms.employmentsupport.domain.CourseVO;
import com.goott5.lms.employmentsupport.domain.CustomEmploymentVO;
import com.goott5.lms.employmentsupport.domain.EmploymentDTO;
import com.goott5.lms.employmentsupport.domain.EmploymentVO;
import com.goott5.lms.employmentsupport.domain.PagingRequestDTO;
import com.goott5.lms.employmentsupport.domain.PagingResponseDTO;
import com.goott5.lms.employmentsupport.service.EmploymentSupportService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/learnerManagement/employment")
public class EmploymentSupportController {

  private final EmploymentSupportService employmentSupportService;

  @GetMapping("")
  public String employmentSupport() {return "learnerManagement/employmentSupport";}

  @GetMapping("/getCoursesByInProgress")
  @ResponseBody
  public List<CourseVO> getCoursesByInProgress (int inProgressType, HttpSession session) {

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    return employmentSupportService.getCoursesByInProgress(inProgressType, loginUser);

  }

  @GetMapping("/getEmploymentList")
  @ResponseBody
  public PagingResponseDTO<CustomEmploymentVO> getEmploymentList(PagingRequestDTO pagingRequestDTO){

    return employmentSupportService.getEmploymentList(pagingRequestDTO);

  }

  @GetMapping("/getEmploymentById")
  @ResponseBody
  public EmploymentVO getEmploymentById(int id){

    return employmentSupportService.getEmploymentById(id);
  }

  @PostMapping("/modifyEmployment")
  @ResponseBody
  public String modifyEmployment(@RequestBody EmploymentDTO employmentDTO){

    if(employmentSupportService.modifyEmployment(employmentDTO)){
      return "success";
    }

    return "fail";


  }

}
