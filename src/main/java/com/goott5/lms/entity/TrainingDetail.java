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
  name = "training_detail",
  uniqueConstraints = @UniqueConstraint(columnNames = "training_id")
)
public class TrainingDetail {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_id", nullable = false, unique = true, referencedColumnName = "id")
  private TrainingLog trainingLog;

  @Column(nullable = false)
  private Integer period;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan", nullable = false, referencedColumnName = "id")
  private CourseSchedule plan;

  @Column(length = 1000)
  private String actual;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
