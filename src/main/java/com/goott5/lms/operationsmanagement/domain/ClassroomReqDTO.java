package com.goott5.lms.operationsmanagement.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class ClassroomReqDTO {
  private Integer id;                    // id
  private String name;                   // name
  private Boolean isActive;              // is_active (tinyint(1) → Boolean)
  private Integer primaryAdminId;        // primary_admin_id
  private Integer secondaryAdminId;      // secondary_admin_id
  private LocalDateTime createdAt;       // created_at
  private LocalDateTime updatedAt;       // updated_at
  private LocalDateTime deletedAt;       // deleted_at
}
