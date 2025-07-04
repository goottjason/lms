package com.goott5.lms.courseboardqna.domain.detail;

import com.goott5.lms.common.domain.FileSelectDTO;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class QnADetailVO {

  private int id;
  private String loginId;
  private String fullName;
  private int writerId;
  private String title;
  private String content;
  private String comment;
  private Boolean isAnswer;
  private Boolean isSecret;
  private LocalDate createdAt;
  private LocalDate commentCreatedAt;
  private List<FileSelectDTO> uploadFiles;

  public boolean getIsAnswer() {
    return isAnswer;
  }

  public void setIsAnswer(boolean isAnswer) {
    this.isAnswer = isAnswer;
  }


  public boolean getIsSecret() {
    return isSecret;
  }

  public void setIsSecret(boolean isSecret) {
    this.isSecret = isSecret;
  }

}
