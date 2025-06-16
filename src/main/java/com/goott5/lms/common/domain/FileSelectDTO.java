package com.goott5.lms.common.domain;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileSelectDTO {

    private int id;
    private String originalName;
    private String newName;
    private String path;
    private Boolean isImage;

    @Size(max = 1024 * 1024 * 8, message = "파일 사이즈가 초과되었습니다.")
    private Integer size;


}
