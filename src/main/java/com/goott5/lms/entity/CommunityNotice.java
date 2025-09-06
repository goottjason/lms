package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "community_notice")
public class CommunityNotice {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 1000)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer", nullable = false, referencedColumnName = "id")
  private User writer;

  @Column(name = "is_notification", nullable = false)
  @ColumnDefault("0")
  private Integer isNotification = 0;

  @Column(name = "is_posted", nullable = false)
  @ColumnDefault("1")
  private Integer isPosted = 1;

  @Column(name = "read_count", nullable = false)
  @ColumnDefault("0")
  private Integer readCount = 0;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
