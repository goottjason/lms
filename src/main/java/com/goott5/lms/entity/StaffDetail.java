package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
  name = "staff_detail",
  uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
public class StaffDetail {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // 외래키 참조 (User)
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true, referencedColumnName = "id")
  private User user;

  @Column(nullable = false, length = 20)
  private String position;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(name = "leave_date")
  private LocalDate leaveDate;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
