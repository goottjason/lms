package com.goott5.lms.operationsmanagement.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ClassroomRespDTO {

  private Integer id;                  // cr.id
  private String name;              // cr.name
  private Boolean isActive;         // cr.is_active
  private Integer primaryAdminId;      // cr.primary_admin_id
  private Integer secondaryAdminId;    // cr.secondary_admin_id
  private String courseName;        // c.name AS courseName
  private Boolean courseIsInProgress; // c.is_in_progress AS courseIsInProgress
  private Integer priUserId;           // u_pri.id AS priUserId
  private String priUserFullname;   // u_pri.fullname AS priUserFullname
  private Integer secUserId;           // u_sec.id AS secUserId
  private String secUserFullname;   // u_sec.fullname AS secUserFullname

}
