package com.goott5.lms.training.domain;

import jakarta.annotation.Nullable;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

@Data
@Builder
public class SelectTrainingForListDTO {

  private Integer id;
  private Integer courseId;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date trainingDate;
  private Integer instructorId;
  @Nullable
  private Boolean hasFile; //null 이거나 id와 같거나

}
