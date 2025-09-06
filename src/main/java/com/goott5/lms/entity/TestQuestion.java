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
  name = "test_question",
  uniqueConstraints = @UniqueConstraint(columnNames = { "test_id", "question_no" })
)
public class TestQuestion {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_id", nullable = false, referencedColumnName = "id")
  private Test test;

  @Column(nullable = false, length = 1000)
  private String title;

  @Column(name = "question_type", nullable = false, length = 10)
  private String questionType;

  @Column(name = "question_no", nullable = false)
  private Integer questionNo;

  @Column(nullable = false)
  private Integer score;

  @Column(name = "correct_answer", nullable = false, length = 1000)
  private String correctAnswer;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
