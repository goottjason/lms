package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test")
public class Test {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instructor_id", nullable = false, referencedColumnName = "id")
  private User instructor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false, referencedColumnName = "id")
  private Course course;

  @Column(nullable = false, length = 100)
  private String title;

  @Column
  private LocalDateTime regdate;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Column(name = "test_time", nullable = false)
  private Integer testTime;

  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @Column(name = "created_at", nullable = false)
  @org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // 👉 추가: 자동 채점 여부 컬럼
  @Column(name = "auto_graded", nullable = false)
  @org.hibernate.annotations.ColumnDefault("false")
  private Boolean autoGraded = false;
}
