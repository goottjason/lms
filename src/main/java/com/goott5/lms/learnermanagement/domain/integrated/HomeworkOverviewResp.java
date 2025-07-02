package com.goott5.lms.learnermanagement.domain.integrated;
import com.goott5.lms.learnermanagement.domain.table.HomeworkWithSubEval;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkOverviewResp<HomeworkWithSubEval> {
  private List<HomeworkWithSubEval> homeList;
  private Integer totalCount;
  private Double homeworkPassRate;
  private Boolean isCompletionAboutHomework;
}
