package com.goott5.lms.courseboardqna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DummyQnADataGenerator {

  public static void main(String[] args) {
    String jdbcUrl      = "jdbc:mysql://amorparami.cafe24.com:3306/amorparami"
        + "?useSSL=false&allowPublicKeyRetrieval=true"
        + "&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    String dbUser       = "amorparami";
    String dbPassword   = "goott!@345";

    // course_qna 테이블에 맞춘 INSERT 문
    String sql = "INSERT INTO course_qna "
        + "(course_id, writer_id, title, content, is_secret) "
        + "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      for (int i = 1; i <= 1000; i++) {
        int courseId    = 38;                           // 예시 course_id
        int writerId    = 36;                            // 예시 writer_id
        String title    = "더미 QnA 제목 테스트 " + i;
        String content  = "더미 QnA 내용 " + i;
        // 매 10번째 글은 비밀글로 표시
        int isSecret = (i % 10 == 0) ? 0 : 1;

        pstmt.setInt(1, courseId);
        pstmt.setInt(2, writerId);
        pstmt.setString(3, title);
        pstmt.setString(4, content);
        pstmt.setInt(5, isSecret);

        pstmt.addBatch();
      }

      pstmt.executeBatch();
      conn.commit();
      System.out.println("1000개의 더미 QnA 데이터가 성공적으로 삽입되었습니다.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
