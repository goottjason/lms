package com.goott5.lms.homework.readcountlog.domain;

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
