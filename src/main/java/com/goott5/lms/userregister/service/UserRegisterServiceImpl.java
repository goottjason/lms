package com.goott5.lms.userregister.service;

import com.goott5.lms.userregister.domain.UserDTO;
import com.goott5.lms.userregister.mapper.UserRegisterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRegisterServiceImpl implements UserRegisterService {

  private final UserRegisterMapper userRegisterMapper;

  @Override
  public boolean checkEmail(String inputEmail) {

    int countOfDuplicate = userRegisterMapper.selectUserCountByEmail(inputEmail);

    if(countOfDuplicate > 0) {
      return true;
    }

    return false;
  }

  @Override
  public boolean saveUser(UserDTO userDTO) {

    boolean result = true;

    if(userRegisterMapper.insertUser(userDTO) < 1){
      result = false;
    };

    if(userDTO.getType().equals("INSTRUCTOR") || userDTO.getType().equals("ADMINISTRATOR")) {

      if(userRegisterMapper.insertStaffDetail(userDTO) < 1){
        result = false;
      }

    }

    return result;
  }
}
