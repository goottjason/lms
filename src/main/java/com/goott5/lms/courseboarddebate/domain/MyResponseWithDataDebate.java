package com.goott5.lms.courseboarddebate.domain;

import lombok.Data;

@Data
public class MyResponseWithDataDebate {

  int code;
  Object data;
  String message;

  public MyResponseWithDataDebate(int code, String message, Object data) {
    this.code = code;
    this.data = data;
    this.message = message;
  }

}
