package com.goott5.lms.learnermanagement.domain.integrated;
import com.goott5.lms.learnermanagement.domain.table.TestWithSub;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOverviewResp<TestWithSub> {
  private List<TestWithSub> testList;
  private Integer totalCount;
}
