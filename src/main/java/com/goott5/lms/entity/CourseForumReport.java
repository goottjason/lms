package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_forum_report")
public class CourseForumReport {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // 외래키: course_forum_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_forum_id", nullable = false, referencedColumnName = "id")
  private CourseForum courseForum;

  // 외래키: user_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  private User user;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
