package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "homework_submission")
public class HomeworkSubmission {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homework_id", nullable = false, referencedColumnName = "id")
  private Homework homework;

  @Column(length = 100)
  private String title;

  @Column(length = 1000)
  private String content;

  @Column(name = "read_count", nullable = false)
  @ColumnDefault("0")
  private Integer readCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "learner_id", nullable = false, referencedColumnName = "id")
  private User learner;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
