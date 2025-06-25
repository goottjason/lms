package com.goott5.lms.employmentsupport.service;

import com.goott5.lms.employmentsupport.domain.CourseVO;
import com.goott5.lms.employmentsupport.domain.CustomEmploymentVO;
import com.goott5.lms.employmentsupport.domain.EmploymentDTO;
import com.goott5.lms.employmentsupport.domain.EmploymentVO;
import com.goott5.lms.employmentsupport.domain.PagingRequestDTO;
import com.goott5.lms.employmentsupport.domain.PagingResponseDTO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;

public interface EmploymentSupportService {

  List<CourseVO> getCoursesByInProgress(int inProgressType, UserVO loginUser);

  PagingResponseDTO<CustomEmploymentVO> getEmploymentList(PagingRequestDTO pagingRequestDTO);

  EmploymentVO getEmploymentById(int id);

  boolean modifyEmployment(EmploymentDTO employmentDTO);
}
