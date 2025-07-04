package com.goott5.lms.communitynotice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class communitynoticeTest {

  public static void main(String[] args) {
    String jdbcUrl      = "jdbc:mysql://amorparami.cafe24.com:3306/amorparami"
        + "?useSSL=false&allowPublicKeyRetrieval=true"
        + "&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    String dbUser       = "amorparami";
    String dbPassword   = "goott!@345";

    // course_qna 테이블에 맞춘 INSERT 문
    String sql = "INSERT INTO community_notice "
        + "(title, content, writer) "
        + "VALUES (?, ?, ?)";

    try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      conn.setAutoCommit(false);

      for (int i = 1; i <= 300; i++) {
        int writerId    = 33;                            // 예시 writer_id
        String title    = "더미 공지사항 제목 테스트 " + i;
        String content  = "더미 공지사항 내용 " + i;
        // 매 10번째 글은 비밀글로 표시
        int isSecret = (i % 10 == 0) ? 0 : 1;


        pstmt.setString(1, title);
        pstmt.setString(2, content);
        pstmt.setInt(3, writerId);

        pstmt.addBatch();
      }

      pstmt.executeBatch();
      conn.commit();
      System.out.println("300개의 더미 공지사항 데이터가 성공적으로 삽입되었습니다.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
