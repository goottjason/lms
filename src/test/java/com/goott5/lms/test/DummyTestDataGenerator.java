package com.goott5.lms.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DummyTestDataGenerator {

  public static void main(String[] args) {
    String jdbcUrl = "jdbc:mysql://amorparami.cafe24.com:3306/amorparami?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    String dbUser = "amorparami";
    String dbPassword = "goott!@345";

    String sql = "INSERT INTO test (instructor_id, course_id, title, start_date, end_date, test_time, total_score, created_at) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime baseDate = LocalDateTime.of(2025, 6, 15, 10, 0);

    try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false); // 성능 향상을 위해 트랜잭션 처리

      for (int i = 1; i <= 1000; i++) {
        int instructorId = 7;
        int courseId = 10;
        String title = "더미 시험 " + i;

        LocalDateTime startDate = baseDate.plusDays(i);
        LocalDateTime endDate = startDate.plusMinutes(60);
        int testTime = 60;
        int totalScore = 100;
        String createdAt = LocalDateTime.now().format(dtf);

        pstmt.setInt(1, instructorId);
        pstmt.setInt(2, courseId);
        pstmt.setString(3, title);
        pstmt.setString(4, startDate.format(dtf));
        pstmt.setString(5, endDate.format(dtf));
        pstmt.setInt(6, testTime);
        pstmt.setInt(7, totalScore);
        pstmt.setString(8, createdAt);

        pstmt.addBatch(); // 일괄 처리로 모아두기
      }

      pstmt.executeBatch(); // 한 번에 실행
      conn.commit();

      System.out.println("1000개의 더미 데이터가 성공적으로 삽입되었습니다.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
