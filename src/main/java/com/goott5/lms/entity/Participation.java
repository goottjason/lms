package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "participation")
public class Participation {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "learner_enrollment_id", nullable = false, referencedColumnName = "id")
  private LearnerEnrollment learnerEnrollment;

  @Column(nullable = false, length = 15)
  private String status;

  @Column(name = "check_in", nullable = false)
  private LocalDateTime checkIn;

  @Column(name = "check_out", nullable = false)
  private LocalDateTime checkOut;

  @Column(name = "training_time", nullable = false)
  private Integer trainingTime;

  @Column(name = "participation_date", nullable = false)
  private LocalDate participationDate;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
