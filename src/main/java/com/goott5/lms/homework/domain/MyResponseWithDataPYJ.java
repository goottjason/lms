package com.goott5.lms.homework.domain;

import lombok.Data;

@Data
public class MyResponseWithDataPYJ {

  int code;
  Object data;
  String message;

  public MyResponseWithDataPYJ(int code, String message, Object data) {
    this.code = code;
    this.data = data;
    this.message = message;
  }

}
