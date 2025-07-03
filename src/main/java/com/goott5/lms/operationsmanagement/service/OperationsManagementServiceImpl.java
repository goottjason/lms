package com.goott5.lms.operationsmanagement.service;

import com.goott5.lms.learnermanagement.domain.dto.LearnerResponse;
import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomUsageResp;
import com.goott5.lms.operationsmanagement.domain.IntegratedReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.StaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.dto.PageStaffRequest;
import com.goott5.lms.operationsmanagement.domain.dto.PageStaffResponse;
import com.goott5.lms.operationsmanagement.domain.dto.StaffResponse;
import com.goott5.lms.operationsmanagement.domain.integrated.StaffOverviewResp;
import com.goott5.lms.operationsmanagement.mapper.OperationsManagementMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationsManagementServiceImpl implements OperationsManagementService {

  private final OperationsManagementMapper operationsManagementMapper;

  @Override
  public PageStaffRespDTO<StaffRespDTO> getStaffsAllorOne(
      BaseReqDTO baseReqDTO,
      PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO
  ) {

    // page정보를 임시 변수에 저장
    Integer originalPageNo = pageStaffReqDTO.getPageNo();
    Integer originalPageSize = pageStaffReqDTO.getPageSize();

    // page정보가 있으면, 임시로 null로 바꿈
    if (originalPageNo != null && originalPageSize != null) {
      pageStaffReqDTO.setPageNo(null);
      pageStaffReqDTO.setPageSize(null);
    }

    // 전체의 레코드 개수를 셈
    List<StaffRespDTO> allStaffs
        = operationsManagementMapper.selectStaffsAllorOne(
            baseReqDTO, pageStaffReqDTO
    );
    Integer totalRecords = allStaffs.size();

    // page정보가 있으면, 임시 변수에서 기존의 page 정보를 꺼내어 페이징된 데이터만 조회
    if (originalPageNo != null && originalPageSize != null) {
      pageStaffReqDTO.setPageNo(originalPageNo);
      pageStaffReqDTO.setPageSize(originalPageSize);
      allStaffs = operationsManagementMapper.selectStaffsAllorOne(
          baseReqDTO, pageStaffReqDTO
      );
    }

    return PageStaffRespDTO.<StaffRespDTO>withPageInfo()
        .pageStaffReqDTO(pageStaffReqDTO)
        .totalRecords(totalRecords)
        .staffRepsDTOS(allStaffs)
        .build();
  }

  @Override
  public PageClassroomRespDTO<ClassroomRespDTO> getClassroomsAllorOne(
      BaseReqDTO baseReqDTO,
      PageClassroomReqDTO<ClassroomReqDTO> pageClassroomReqDTO) {

    // page정보를 임시 변수에 저장
    Integer originalPageNo = pageClassroomReqDTO.getPageNo();
    Integer originalPageSize = pageClassroomReqDTO.getPageSize();

    // page정보가 있으면, 임시로 null로 바꿈
    if (originalPageNo != null && originalPageSize != null) {
      pageClassroomReqDTO.setPageNo(null);
      pageClassroomReqDTO.setPageSize(null);
    }

    // 전체의 레코드 개수를 셈
    List<ClassroomRespDTO> allClassrooms
        = operationsManagementMapper.selectClassroomsAllorOne(
        baseReqDTO, pageClassroomReqDTO
    );
    Integer totalRecords = allClassrooms.size();

    // page정보가 있으면, 임시 변수에서 기존의 page 정보를 꺼내어 페이징된 데이터만 조회
    if (originalPageNo != null && originalPageSize != null) {
      pageClassroomReqDTO.setPageNo(originalPageNo);
      pageClassroomReqDTO.setPageSize(originalPageSize);
      allClassrooms = operationsManagementMapper.selectClassroomsAllorOne(
          baseReqDTO, pageClassroomReqDTO
      );
    }

    return PageClassroomRespDTO.<ClassroomRespDTO>withPageInfo()
        .pageClassroomReqDTO(pageClassroomReqDTO)
        .totalRecords(totalRecords)
        .classroomRepsDTOS(allClassrooms)
        .build();
  }

  @Override
  public Boolean updateClassroom(IntegratedReqDTO integratedReqDTO) {
    int result = operationsManagementMapper.updateClassroom(
        integratedReqDTO.getBaseReqDTO(),
        integratedReqDTO.getClassroomReqDTO());
    if (result == 1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean deleteClassroom(IntegratedReqDTO integratedReqDTO) {
    int result = operationsManagementMapper.deleteClassroom(
        integratedReqDTO.getBaseReqDTO(),
        integratedReqDTO.getClassroomReqDTO());
    if (result == 1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Boolean addClassroom(IntegratedReqDTO integratedReqDTO) {
    String loginUserType = integratedReqDTO.getBaseReqDTO().getLoginUserType();
    int result = 0;
    if (loginUserType != null
        && "ADMINISTRATOR".equals(loginUserType)) {
      result = operationsManagementMapper.insertClassroom(
          integratedReqDTO.getBaseReqDTO(),
          integratedReqDTO.getClassroomReqDTO());
      return true;
    }
    return false;
  }

  @Override
  public Boolean updateLeaveDateByStaffId(Integer loginUserId, String loginUserType,
      Integer staffId, String leaveDate) {

    int result = 0;

    if (loginUserType != null && "ADMINISTRATOR".equals(loginUserType)) {
      result = operationsManagementMapper.updateLeaveDate(
          staffId, leaveDate
      );
      return true;
    }
    return false;
  }

  @Override
  public Boolean modifyClassroomIsActiveBycoClassroomId(Integer coClassroomId) {
    return operationsManagementMapper.updateClassroomIsActiveBycoClassroomId(coClassroomId);
  }

  @Override
  public List<ClassroomUsageResp> getClassroomUsage() {
    return operationsManagementMapper.selectClassroomUsage();
  }

  @Override
  public PageStaffResponse<StaffResponse> getStaffsByAuth(
      BaseReqDTO baseReqDTO,
      PageStaffRequest pageStaffRequest) {

    Integer originalPageNo = pageStaffRequest.getPageNo();
    Integer originalPageSize = pageStaffRequest.getPageSize();
    if (originalPageNo != null && originalPageSize != null) {
      pageStaffRequest.setPageNo(null);
      pageStaffRequest.setPageSize(null);
    }
    List<StaffResponse> staffs = operationsManagementMapper.selectStaffsByAuth(
        baseReqDTO, pageStaffRequest);
    Integer totalRecords = staffs.size();

    if (originalPageNo != null && originalPageSize != null) {
      pageStaffRequest.setPageNo(originalPageNo);
      pageStaffRequest.setPageSize(originalPageSize);
      staffs = operationsManagementMapper.selectStaffsByAuth(
          baseReqDTO, pageStaffRequest);
    }
    return PageStaffResponse.<StaffResponse>withPageInfo()
        .request(pageStaffRequest)
        .totalRecords(totalRecords)
        .records(staffs)
        .build();
  }
}
