package com.goott5.lms.common.domain;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileDTO {

    private int id;
    private String originalName;
    private String newName;
    private String path;
    private Boolean isImage;

    @Size(max = 1024 * 1024 * 8, message = "파일 사이즈가 초과되었습니다.")
    private Integer size;

    private String tableName;
    private int tableId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}
