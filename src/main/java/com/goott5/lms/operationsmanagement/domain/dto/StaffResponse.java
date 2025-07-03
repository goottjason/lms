package com.goott5.lms.operationsmanagement.domain.dto;

import com.goott5.lms.operationsmanagement.domain.StaffHistoryReq;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponse {
  // 셀렉트문 조회해서 담아올 컬럼들...
  private Integer userId;
  private String type;
  private String loginId;
  private String password;
  private String fullname;
  private String gender;
  private LocalDate birthday;
  private String mobile;
  private String email;
  private String address;
  private String profileImg;
  private String sessionId;
  private LocalDateTime autoLoginLimit;
  private Integer wrongPasswordCount;


  private String position;
  private LocalDate hireDate;
  private LocalDate leaveDate;


  private String courseIdList;
  private String courseIsInProgressList;
  private String courseNameList;
  private String courseNumberOfLearnerList;
  private String courseStartDateList;
  private String courseEndDateList;

  private List<StaffHistoryReq> assignmentHistoryList;

  public List<StaffHistoryReq> getAssignmentHistoryList() {
    if(courseIdList == null) { return assignmentHistoryList; }

    List<StaffHistoryReq> assignmentHistoryList = new ArrayList<>();
    String[] ids = courseIdList.split(",");
    String[] isInProgress = courseIsInProgressList.split(",");
    String[] nameList = courseNameList.split(",");
    String[] numberOfLearnerList = courseNumberOfLearnerList.split(",");
    String[] startDateList = courseStartDateList.split(",");
    String[] endDateList = courseEndDateList.split(",");
    for(int i = 0; i < ids.length; i++) {
      StaffHistoryReq staffHistoryReq = new StaffHistoryReq();

      staffHistoryReq.setCourseId(Integer.parseInt(ids[i].trim()));
      staffHistoryReq.setCourseIsInProgress("1".equals(isInProgress[i].trim()));
      staffHistoryReq.setCourseName(nameList[i].trim());
      staffHistoryReq.setCourseNumberOfLearner(Integer.parseInt(numberOfLearnerList[i].trim()));
      staffHistoryReq.setCourseStartDate(LocalDate.parse(startDateList[i].trim()));
      staffHistoryReq.setCourseEndDate(LocalDate.parse(endDateList[i].trim()));

      assignmentHistoryList.add(staffHistoryReq);
    }
    return assignmentHistoryList;
  }
}
