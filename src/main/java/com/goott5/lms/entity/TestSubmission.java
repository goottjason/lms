package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
  name = "test_submission",
  uniqueConstraints = @UniqueConstraint(columnNames = { "test_id", "learner_id" })
)
public class TestSubmission {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_id", referencedColumnName = "id")
  private Test test;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "learner_id", referencedColumnName = "id")
  private User learner;

  @Column(name = "submission_time")
  private Integer submissionTime;

  @Column(name = "submission_status", nullable = false, length = 15)
  @ColumnDefault("'NOT_STARTED'")
  private String submissionStatus = "NOT_STARTED";

  @Column(name = "retry_count", nullable = false)
  @ColumnDefault("0")
  private Integer retryCount = 0;

  @Column(name = "is_invalidated", nullable = false)
  @ColumnDefault("0")
  private Integer isInvalidated = 0;

  @Column(name = "submission_reg_date")
  private LocalDateTime submissionRegDate;

  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer score = 0;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
