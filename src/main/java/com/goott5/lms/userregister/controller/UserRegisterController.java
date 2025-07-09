package com.goott5.lms.userregister.controller;

import com.goott5.lms.userregister.domain.UserDTO;
import com.goott5.lms.userregister.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/operationsManagement/userRegister")
public class UserRegisterController {

  private final UserRegisterService userRegisterService;

  @GetMapping("")
  public String userRegister() {
    return "operationsManagement/userRegister";
  }

  @GetMapping("/checkEmail")
  @ResponseBody
  public String checkEmail(String inputEmail) {

//    log.info("inputEmail:{}", inputEmail);
    boolean isDuplicate = userRegisterService.checkEmail(inputEmail);

    if(isDuplicate) {
      return "duplicateEmail";
    } else {
      return "availableEmail";
    }
  }

  @PostMapping("/saveUser")
  public String saveUser(UserDTO userDTO, RedirectAttributes redirectAttributes) {

//    log.info("userDTO:{}", userDTO);

    boolean isSuccess = userRegisterService.saveUser(userDTO);

    redirectAttributes.addFlashAttribute("isSaveSuccess", isSuccess);

    return "redirect:/operationsManagement/userRegister";

  }

}
