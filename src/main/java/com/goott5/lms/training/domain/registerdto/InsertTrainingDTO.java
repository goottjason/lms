package com.goott5.lms.training.domain.registerdto;

import java.time.LocalDate;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@Data
public class InsertTrainingDTO {

  private int courseId;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate trainingDate;
  private int instructorId;

}
