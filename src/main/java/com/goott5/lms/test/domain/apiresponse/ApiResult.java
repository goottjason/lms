package com.goott5.lms.test.domain.apiresponse;

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
public class ApiResult<T> {

  private int code;
  private String message;
  private T data;

  public static <T> ApiResult<T> success(int code, String message, T data) {
    return new ApiResult<T>(code, message, data);
  }

  public static <T> ApiResult<T> fail(int code, String message, T data) {
    return new ApiResult<T>(code, message, data);
  }

  public static <T> ResponseEntity<ApiResult<T>> respondOk(int code, String message, T data) {
    return ResponseEntity.ok(success(code, message, data));
  }

  public static <T> ResponseEntity<ApiResult<T>> respondFail(int code, String message, T data,
          HttpStatus status) {
    return ResponseEntity.status(status).body(fail(code, message, data));
  }

}
