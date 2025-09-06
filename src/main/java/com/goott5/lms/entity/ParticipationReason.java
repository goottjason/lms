package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "participation_reason")
public class ParticipationReason {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "participation_id", referencedColumnName = "id")
  private Participation participation;

  @Column(nullable = false, length = 1000)
  private String explanation;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
