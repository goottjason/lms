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
  name = "test_answer",
  uniqueConstraints = @UniqueConstraint(columnNames = { "question_id", "submission_id" })
)
public class TestAnswer {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false, referencedColumnName = "id")
  private TestQuestion question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false, referencedColumnName = "id")
  private TestSubmission submission;

  @Column(name = "select_answer", nullable = false, length = 1000)
  private String selectAnswer;

  @Column(name = "is_correct", nullable = false)
  @ColumnDefault("0")
  private Integer isCorrect = 0;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
