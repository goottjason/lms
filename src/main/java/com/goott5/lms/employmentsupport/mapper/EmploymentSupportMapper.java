package com.goott5.lms.employmentsupport.mapper;

import com.goott5.lms.employmentsupport.domain.CourseVO;
import com.goott5.lms.employmentsupport.domain.CustomEmploymentVO;
import com.goott5.lms.employmentsupport.domain.EmploymentDTO;
import com.goott5.lms.employmentsupport.domain.EmploymentVO;
import com.goott5.lms.employmentsupport.domain.PagingRequestDTO;
import com.goott5.lms.employmentsupport.domain.PagingResponseDTO;
import com.goott5.lms.user.domain.UserVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmploymentSupportMapper {

  @Select("select sd.position from user u join staff_detail sd on u.id = sd.user_id where u.id = #{id}")
  String selectUserPosition(UserVO loginUser);

  List<CourseVO> selectCoursesByInProgress(int inProgressType);

  List<CourseVO> selectCoursesByInProgressAndLoginUser(@Param("inProgressType") int inProgressType, @Param("loginUser") UserVO loginUser);

  List<CustomEmploymentVO> selectEmploymentList(PagingRequestDTO pagingRequestDTO);

  int selectCountOfEmployment(PagingRequestDTO pagingRequestDTO);

  @Select("select id, learner_enrollment_id, is_counseling_received, counseling_details, employment_status, company_name, company_phone, company_address from employment_support where id = #{id}")
  EmploymentVO selectEmploymentById(int id);

  @Update("update employment_support set is_counseling_received = #{isCounselingReceived}, counseling_details = #{counselingDetails}, employment_status = #{employmentStatus}, company_name = #{companyName}, company_phone = #{companyPhone}, company_address = #{companyAddress} where id = #{id}")
  int updateEmployment(EmploymentDTO employmentDTO);
}
