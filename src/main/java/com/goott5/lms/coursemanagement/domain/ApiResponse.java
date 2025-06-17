package com.goott5.lms.coursemanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ApiResponse<T> {

  private int code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(
          int code,
          String message,
          T data) {
    return new ApiResponse<T>(code, message, data);
    // ApiResponse.success(200, "등록 성공", resultData);
  }

  public static <T> ApiResponse<T> fail(
          int code,
          String message,
          T data) {
    return new ApiResponse<T>(code, message, data);
    // ApiResponse.fail(400, "등록 실패", errorDetails);
  }

  public static <T> ResponseEntity<ApiResponse<T>> okResponse(
          int code,
          String message,
          T data) {
    return ResponseEntity.ok(success(code, message, data));
    // ApiResponse.okResponse(200, "등록 성공", resultData);
  }

  public static <T> ResponseEntity<ApiResponse<T>> failResponse(
          int code,
          String message,
          T data,
          HttpStatus status) {
    return ResponseEntity.status(status).body(fail(code, message, data));
    // ApiResponse.failResponse(400, "등록 실패", errorDetails, HttpStatus.BAD_REQUEST);
  }
}
