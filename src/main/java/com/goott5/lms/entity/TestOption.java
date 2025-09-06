package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_option")
public class TestOption {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false, referencedColumnName = "id")
  private TestQuestion question;

  @Column(name = "option_no", nullable = false)
  private Integer optionNo;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(name = "created_at", nullable = false)
  @org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
