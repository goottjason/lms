package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "community_inquiry")
public class CommunityInquiry {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 1000)
  private String inquiry;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer", nullable = false, referencedColumnName = "id")
  private User writer;

  @Column(name = "is_posted", nullable = false)
  @ColumnDefault("1")
  private Integer isPosted = 1;

  @Column(name = "is_answered", nullable = false)
  @ColumnDefault("0")
  private Integer isAnswered = 0;

  @Column(name = "is_answer_checked", nullable = false)
  @ColumnDefault("0")
  private Integer isAnswerChecked = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "answerer", referencedColumnName = "id")
  private User answerer;

  @Column(length = 1000)
  private String answer;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;
}
