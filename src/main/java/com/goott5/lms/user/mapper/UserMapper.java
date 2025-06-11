package com.goott5.lms.user.mapper;

import com.goott5.lms.user.domain.SignupDTO;
import com.goott5.lms.user.domain.UserVO;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

  @Select("select id, type, login_id, password, fullname, gender, birthday, mobile, email, address, "
          + "profile_img, session_id, auto_login_limit, wrong_password_count, created_at, updated_at, deleted_at from user where login_id = #{loginId}")
  UserVO selectUserByLoginId(@Param("loginId") String loginId);

  @Update("update user set wrong_password_count = wrong_password_count + 1 where id = #{id}")
  void updateUserWrongPasswordCount(@Param("id") String id);

  @Update("update user set login_id = #{loginId}, password = #{password}, mobile = #{mobile}, email = #{email}, "
          + "address = #{address}, profile_img = #{profileImg}, updated_at = now() where id = #{userId}")
  int updateUserForSignup(SignupDTO signupDTO);

  @Update("update user set session_id = #{sessionId}, auto_login_limit = #{localDateTime} where id = #{userId}")
  int updateUserAutoLogin(@Param("userId") String userId, @Param("sessionId") String sessionId,
          @Param("localDateTime") LocalDateTime localDateTime);
}