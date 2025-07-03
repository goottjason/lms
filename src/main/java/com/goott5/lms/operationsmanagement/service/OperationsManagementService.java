package com.goott5.lms.operationsmanagement.service;

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
import java.time.LocalDate;
import java.util.List;


public interface OperationsManagementService {

  PageStaffRespDTO<StaffRespDTO> getStaffsAllorOne(
      BaseReqDTO baseReqDTO,
      PageStaffReqDTO<StaffReqDTO> pageStaffReqDTO);

  PageClassroomRespDTO<ClassroomRespDTO> getClassroomsAllorOne(
      BaseReqDTO baseReqDTO,
      PageClassroomReqDTO<ClassroomReqDTO> pageClassroomReqDTO);

  Boolean updateClassroom(IntegratedReqDTO integratedReqDTO);

  Boolean deleteClassroom(IntegratedReqDTO integratedReqDTO);

  Boolean addClassroom(IntegratedReqDTO integratedReqDTO);

  Boolean updateLeaveDateByStaffId(Integer loginUserId, String loginUserType, Integer staffId, String leaveDate);

  Boolean modifyClassroomIsActiveBycoClassroomId(Integer coClassroomId);

  List<ClassroomUsageResp> getClassroomUsage();

  PageStaffResponse<StaffResponse> getStaffsByAuth(BaseReqDTO baseReqDTO, PageStaffRequest pageStaffRequest);
}
