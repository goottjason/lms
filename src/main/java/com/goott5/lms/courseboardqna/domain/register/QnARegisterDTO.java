package com.goott5.lms.courseboardqna.domain.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
  @Size(max = 100, message = "제목을 100자 이하로 입력해주세요.")
  private String title;

  @NotBlank(message = "내용은 필수 항목입니다.")
  @Size(max = 1000, message = "내용은 1000자 이하로 입력해주세요.")
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
