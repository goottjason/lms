package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
  name = "course",
  uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Course {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "is_in_progress", nullable = false)
  @ColumnDefault("1")
  private Integer isInProgress = 1;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(name = "number_of_learner", nullable = false)
  private Integer numberOfLearner;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "total_hours", nullable = false)
  private Integer totalHours;

  @Column(name = "total_days", nullable = false)
  private Integer totalDays;

  @Column(name = "daily_hours", nullable = false)
  private Integer dailyHours;

  @Column(name = "break_time", nullable = false)
  private Integer breakTime;

  @Column(name = "lesson_start_time", nullable = false)
  private LocalTime lessonStartTime;

  @Column(name = "lesson_end_time", nullable = false)
  private LocalTime lessonEndTime;

  @Column(name = "lunch_start_time", nullable = false)
  private LocalTime lunchStartTime;

  @Column(name = "lunch_end_time", nullable = false)
  private LocalTime lunchEndTime;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
