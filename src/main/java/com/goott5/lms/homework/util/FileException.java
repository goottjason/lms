package com.goott5.lms.homework.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileException extends RuntimeException{

  private int statusCode;
  private String message;
  private Object error;

  public FileException(int statusCode, String message, Object error) {
    this.statusCode = statusCode;
    this.message = message;
    this.error = error;
  }


}
