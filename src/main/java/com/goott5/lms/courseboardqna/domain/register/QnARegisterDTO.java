package com.goott5.lms.courseboardqna.domain.register;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class QnARegisterDTO {

  @NotBlank(message = "제목은 필수 항목입니다.")
  private String title;

  @NotBlank(message = "내용은 필수 항목입니다.")
  private String content;
  private boolean isSecret;

  private String courseName;

  private List<MultipartFile> uploadFiles;
  private List<Long> removedFileIds;

  public boolean getIsSecret() {
    return isSecret;
  }

  public void setIsSecret(boolean isSecret) {
    this.isSecret = isSecret;
  }

}
