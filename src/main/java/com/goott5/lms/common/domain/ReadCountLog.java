package com.goott5.lms.common.domain;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReadCountLog {

  private int userId;
  private String tableName;
  private int tableId;
  private Date readDate;


}
