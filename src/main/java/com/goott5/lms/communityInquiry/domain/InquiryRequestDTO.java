package com.goott5.lms.communityInquiry.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class InquiryRequestDTO {

  private int id;

  @NotBlank(message = "제목은 필수 입력 입니다.")
  @Size(max = 100, message = "제목은 100자까지 입력 가능합니다.")
  private String title;

  private String inquiry;
  private int writer;


}
