package com.goott5.lms.courseboardqna.domain.register;

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
public class QnARegisterVO {

  private int id;
  private int courseId;
  private int writerId;
  private String title;
  private String content;
  private boolean isAnswer;
  private boolean isSecret;

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
