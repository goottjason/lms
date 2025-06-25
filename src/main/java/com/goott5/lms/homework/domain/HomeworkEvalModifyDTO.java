package com.goott5.lms.homework.domain;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Builder
@Data
public class HomeworkEvalModifyDTO {

    private Integer id;

    @NotNull(message = "통과 여부는 필수로 입력해주세요.")
    private Boolean isPass;

    private String content;

    private LocalDateTime updatedAt;


}
