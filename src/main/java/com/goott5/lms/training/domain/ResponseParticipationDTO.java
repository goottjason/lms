package com.goott5.lms.training.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseParticipationDTO {

  private int attendanceCount;
  private int lateCount;
  private int absenceCount;
  private int leaveEarLyCount;
  private int vacationPaddingCount;

  private List<String> attendanceList;
  private List<String> lateList;
  private List<String> absenceList;
  private List<String> leaveEarLyList;
  private List<String> vacationPaddingList;

}
