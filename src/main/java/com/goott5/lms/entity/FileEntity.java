package com.goott5.lms.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file")
public class FileEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "original_name", nullable = false, length = 100)
  private String originalName;

  @Column(name = "new_name", nullable = false, length = 200)
  private String newName;

  @Column(nullable = false, length = 500)
  private String path;

  @Column(name = "is_image", nullable = false)
  @ColumnDefault("0")
  private Integer isImage = 0;

  @Column(nullable = false)
  private Integer size;

  @Column(name = "table_name", nullable = false, length = 30)
  private String tableName;

  @Column(name = "table_id", nullable = false)
  private Integer tableId;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
