package com.goott5.lms.courseboardmaterials.domain;

import lombok.Data;

@Data
public class MyResponseWithData {

  int code;
  Object data;
  String message;

  public MyResponseWithData(int code, String message, Object data) {
    this.code = code;
    this.data = data;
    this.message = message;
  }

}
