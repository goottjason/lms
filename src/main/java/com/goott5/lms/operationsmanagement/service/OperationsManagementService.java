package com.goott5.lms.operationsmanagement.service;

import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.IntegratedReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffRespDTO;
import com.goott5.lms.operationsmanagement.domain.StaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import java.time.LocalDate;


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
}
