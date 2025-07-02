package com.goott5.lms.training.domain.registerdto;

import com.goott5.lms.training.domain.ResponseParticipationDTO;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@Data
public class SelectAllWithoutActualDTO {

  //과정명 +id
  private SelectCourseDTO selectCourseDTO;

  //훈련일자
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date trainingDate;

  // 총 수강생 수
  private int numberOfLearner;

  // 출결 현황
  private ResponseParticipationDTO responseParticipationDTO;

  // 교시/훈련과목
  private List<SelectSchSubDTO> selectSchSubDTOList;



}
