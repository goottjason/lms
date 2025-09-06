package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cancel_date")
public class CancelDate {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "is_all", nullable = false)
  private Integer isAll;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", referencedColumnName = "id")
  private Course course;

  @Column(name = "is_public_holiday", nullable = false)
  private Integer isPublicHoliday;

  @Column(name = "cancel_date", nullable = false)
  private LocalDate cancelDate;

  @Column(nullable = false, length = 255)
  private String reason;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
