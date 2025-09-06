package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "read_count_log")
public class ReadCountLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  private User user;

  @Column(name = "table_name", nullable = false, length = 30)
  private String tableName;

  @Column(name = "table_id", nullable = false)
  private Integer tableId;

  @Column(name = "read_date", nullable = false)
  private LocalDate readDate;

  @Column(name = "created_at", nullable = false)
  @org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
