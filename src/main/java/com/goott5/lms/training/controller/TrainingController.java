package com.goott5.lms.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/training")
public class TrainingController {

  @GetMapping("/trainingList")
  public String trainingList() {
    return "training/trainingList";
  }

  @GetMapping("/trainingDetail")
  public String trainingDetail() {
    return "training/trainingDetail";
  }

//  @GetMapping("/trainingLogRegister")
//  public String trainingLogRegister() {
//    return "trainingLog/trainingLogRegister";
//  }

}
