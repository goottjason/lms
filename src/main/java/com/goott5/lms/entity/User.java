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
  name = "user",
  indexes = {} // 인덱스 추가 시 이곳에 작성
)
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 15)
  private String type;

  @Column(name = "login_id", nullable = false, unique = true, length = 20)
  private String loginId;

  @Column(length = 100)
  private String password;

  @Column(nullable = false, length = 20)
  private String fullname;

  @Column(nullable = false, length = 1)
  private String gender;

  @Column(nullable = false)
  private LocalDate birthday;

  @Column(unique = true, length = 15)
  private String mobile;

  @Column(nullable = false, unique = true, length = 50)
  private String email;

  @Column(length = 100)
  private String address;

  @Column(name = "profile_img", length = 500)
  private String profileImg;

  @Column(name = "session_id", length = 20)
  private String sessionId;

  @Column(name = "auto_login_limit")
  private LocalDateTime autoLoginLimit;

  @Column(name = "wrong_password_count", nullable = false)
  @ColumnDefault("0")
  private Integer wrongPasswordCount = 0;

  @Column(name = "created_at", nullable = false)
  @ColumnDefault("CURRENT_TIMESTAMP")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
