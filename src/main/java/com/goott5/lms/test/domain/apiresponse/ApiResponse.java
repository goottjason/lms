package com.goott5.lms.test.domain.apiresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;
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

  public static <T> ApiResponse<T> success(int code, String message, T data) {
    return new ApiResponse<T>(code, message, data);
  }

  public static <T> ApiResponse<T> fail(int code, String message, T data) {
    return new ApiResponse<T>(code, message, data);
  }

  public static <T> ResponseEntity<ApiResponse<T>> respondOk(int code, String message, T data) {
    return ResponseEntity.ok(success(code, message, data));
  }

  public static <T> ResponseEntity<ApiResponse<T>> respondFail(int code, String message, T data,
          HttpStatus status) {
    return ResponseEntity.status(status).body(fail(code, message, data));
  }

}
