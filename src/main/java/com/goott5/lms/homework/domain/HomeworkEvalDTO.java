package com.goott5.lms.homework.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@Data
public class HomeworkEvalDTO {

    private int id;
    private int hsId;
    private Boolean isPass;
    private String content;
    private int readCount;
    private int instructorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

}
