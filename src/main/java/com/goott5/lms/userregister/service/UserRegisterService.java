package com.goott5.lms.userregister.service;

import com.goott5.lms.userregister.domain.UserDTO;

public interface UserRegisterService {

  boolean checkEmail(String inputEmail);

  boolean saveUser(UserDTO userDTO);
}
