package com.goott5.lms.report.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportVO {
  // 신고 정보
  private Long id;
  private String reportStatus;
  private String reportDetail;
  private LocalDateTime createdAt;

  // 연관된 게시글 정보
  private int forumId;
  private String postTitle;
  private String postContent;

  // 연관된 과정 정보
  private String courseName;

  // 연관된 사용자 정보
  private String postWriterName;
  private String reporterName;

}
