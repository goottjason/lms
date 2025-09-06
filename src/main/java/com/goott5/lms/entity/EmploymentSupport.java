package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employment_support")
public class EmploymentSupport {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "learner_enrollment_id", nullable = false, referencedColumnName = "id")
  private LearnerEnrollment learnerEnrollment;

  @Column(name = "is_counseling_received", nullable = false)
  @ColumnDefault("0")
  private Integer isCounselingReceived = 0;

  @Column(name = "counseling_details", length = 1000)
  private String counselingDetails;

  @Column(name = "employment_status", nullable = false, length = 15)
  @ColumnDefault("'UNEMPLOYED'")
  private String employmentStatus = "UNEMPLOYED";

  @Column(name = "company_name", length = 100)
  private String companyName;

  @Column(name = "company_phone", length = 100)
  private String companyPhone;

  @Column(name = "company_address", length = 100)
  private String companyAddress;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
