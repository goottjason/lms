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
  name = "classroom",
  uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Classroom {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(name = "is_active", nullable = false)
  @ColumnDefault("0")
  private Integer isActive = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "primary_admin_id", nullable = false, referencedColumnName = "id")
  private User primaryAdmin;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "secondary_admin_id", nullable = false, referencedColumnName = "id")
  private User secondaryAdmin;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
