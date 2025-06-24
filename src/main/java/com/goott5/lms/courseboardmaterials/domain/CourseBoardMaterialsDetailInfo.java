package com.goott5.lms.courseboardmaterials.domain;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.user.domain.UserVO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class CourseBoardMaterialsDetailInfo {

  private int id;
  private int courseId;
  private String courseName;
  private int writerId;
  private String writerName;
  private String writerType;
  private String title;
  private String content;
  private int readCount;
  private Boolean isFixed;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  private Boolean isAttached; // 첨부파일 여부

  private UserVO user; // 작성자 UserVO
  private List<FileSelectDTO> attachments;


}
