package com.goott5.lms.userregister.mapper;

import com.goott5.lms.userregister.domain.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserRegisterMapper {

  @Select("select count(*) from user where email = #{inputEmail}")
  int selectUserCountByEmail(String inputEmail);

  @Insert("insert into user(type, fullname, gender, birthday, email) values(#{type}, #{fullname}, #{gender}, #{birthday}, #{email})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertUser(UserDTO userDTO);

  @Insert("insert into staff_detail(user_id, position, hire_date) values(#{id}, #{position}, #{hireDate})")
  int insertStaffDetail(UserDTO userDTO);
}
