package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_notice")
public class CourseNotice {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false, referencedColumnName = "id")
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id", nullable = false, referencedColumnName = "id")
  private User writer;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(name = "read_count", nullable = false)
  @ColumnDefault("0")
  private Integer readCount = 0;

  @Column(name = "is_fixed", nullable = false)
  @ColumnDefault("0")
  private Integer isFixed = 0;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
