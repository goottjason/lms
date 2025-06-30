package com.goott5.lms.common.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public String handleException(Exception e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    return "layout/error";
  }

}
