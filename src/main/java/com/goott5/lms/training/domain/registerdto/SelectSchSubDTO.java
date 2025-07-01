package com.goott5.lms.training.domain.registerdto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SelectSchSubDTO {

  private int id; //과정 계획 아이디
  private int subjectId; //과목 아이디
  private String name; //과목명

}
