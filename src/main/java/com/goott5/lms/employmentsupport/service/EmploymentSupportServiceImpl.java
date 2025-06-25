package com.goott5.lms.employmentsupport.service;

import com.goott5.lms.employmentsupport.domain.CourseVO;
import com.goott5.lms.employmentsupport.domain.CustomEmploymentVO;
import com.goott5.lms.employmentsupport.domain.EmploymentDTO;
import com.goott5.lms.employmentsupport.domain.EmploymentVO;
import com.goott5.lms.employmentsupport.domain.PagingRequestDTO;
import com.goott5.lms.employmentsupport.domain.PagingResponseDTO;
import com.goott5.lms.employmentsupport.mapper.EmploymentSupportMapper;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class EmploymentSupportServiceImpl implements EmploymentSupportService {

  private final EmploymentSupportMapper employmentSupportMapper;

  @Override
  public List<CourseVO> getCoursesByInProgress(int inProgressType, UserVO loginUser) {

    if(isGeneralManager(loginUser)){

      return employmentSupportMapper.selectCoursesByInProgress(inProgressType);
    }

    return employmentSupportMapper.selectCoursesByInProgressAndLoginUser(inProgressType, loginUser);
  }

  @Override
  public PagingResponseDTO<CustomEmploymentVO> getEmploymentList(PagingRequestDTO pagingRequestDTO) {

    List<CustomEmploymentVO> customEmploymentVOS = employmentSupportMapper.selectEmploymentList(pagingRequestDTO);

    int total = employmentSupportMapper.selectCountOfEmployment(pagingRequestDTO);

    return PagingResponseDTO.<CustomEmploymentVO>allInfo()
            .pagingRequestDTO(pagingRequestDTO)
            .voList(customEmploymentVOS)
            .total(total)
            .build();
  }

  @Override
  public EmploymentVO getEmploymentById(int id) {
    return employmentSupportMapper.selectEmploymentById(id);
  }

  @Override
  public boolean modifyEmployment(EmploymentDTO employmentDTO) {


    if(employmentSupportMapper.updateEmployment(employmentDTO) < 1){
      return false;
    }

    return true;
  }

  public boolean isGeneralManager(UserVO loginUser){

    return ("GENERAL_MANAGER".equals(employmentSupportMapper.selectUserPosition(loginUser)));

  }
}
