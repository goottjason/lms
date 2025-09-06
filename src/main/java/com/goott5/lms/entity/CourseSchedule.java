package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_schedule")
public class CourseSchedule {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_id", nullable = false, referencedColumnName = "id")
  private CourseSubject subject;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false, referencedColumnName = "id")
  private Course course;

  @Column(name = "class_date", nullable = false)
  private LocalDate classDate;

  @Column(nullable = false)
  private Integer period;

  @Column(name = "period_start_time", nullable = false)
  private LocalTime periodStartTime;

  @Column(name = "period_end_time", nullable = false)
  private LocalTime periodEndTime;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
