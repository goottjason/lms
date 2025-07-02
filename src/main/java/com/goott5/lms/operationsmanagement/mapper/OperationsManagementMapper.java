package com.goott5.lms.operationsmanagement.mapper;

import com.goott5.lms.operationsmanagement.domain.BaseReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomRespDTO;
import com.goott5.lms.operationsmanagement.domain.ClassroomUsageResp;
import com.goott5.lms.operationsmanagement.domain.PageClassroomReqDTO;
import com.goott5.lms.operationsmanagement.domain.PageStaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.StaffReqDTO;
import com.goott5.lms.operationsmanagement.domain.StaffRespDTO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OperationsManagementMapper {

  List<StaffRespDTO> selectStaffsAllorOne(
      @Param("base") BaseReqDTO base,
      @Param("page") PageStaffReqDTO<StaffReqDTO> page);

  List<ClassroomRespDTO> selectClassroomsAllorOne(
      @Param("base") BaseReqDTO base,
      @Param("page") PageClassroomReqDTO<ClassroomReqDTO> page);

  int updateClassroom(
      @Param("base") BaseReqDTO base,
      @Param("classroom") ClassroomReqDTO classroom
  );

  int deleteClassroom(
      @Param("base") BaseReqDTO base,
      @Param("classroom") ClassroomReqDTO classroom);

  int insertClassroom(
      @Param("base") BaseReqDTO base,
      @Param("classroom") ClassroomReqDTO classroom);


  int updateLeaveDate(
      @Param("staffId") Integer staffId,
      @Param("leaveDate") String leaveDate);

  @Update("UPDATE classroom cr SET cr.is_active = false WHERE cr.id = #{coClassroomId}")
  Boolean updateClassroomIsActiveBycoClassroomId(Integer coClassroomId);

  List<ClassroomUsageResp> selectClassroomUsage();
}
