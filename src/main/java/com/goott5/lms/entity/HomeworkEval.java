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
  name = "homework_eval",
  uniqueConstraints = @UniqueConstraint(columnNames = "hs_id")
)
public class HomeworkEval {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hs_id", nullable = false, unique = true, referencedColumnName = "id")
  private HomeworkSubmission homeworkSubmission;

  @Column(name = "is_pass", nullable = false)
  @ColumnDefault("0")
  private Integer isPass = 0;

  @Column(length = 1000)
  private String content;

  @Column(name = "read_count", nullable = false)
  @ColumnDefault("0")
  private Integer readCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instructor_id", referencedColumnName = "id")
  private User instructor;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
