package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_subject")
public class CourseSubject {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false, referencedColumnName = "id")
  private Course course;

  @Column(name = "subject_order", nullable = false)
  private Integer subjectOrder;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false)
  private Integer hours;

  @Column(name = "textbook_name", nullable = false, length = 100)
  private String textbookName;

  @Column(name = "textbook_author", nullable = false, length = 100)
  private String textbookAuthor;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
