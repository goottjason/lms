package com.goott5.lms.training.domain.registerdto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SelectCourseDTO {

  private int id;
  private String name;

}
