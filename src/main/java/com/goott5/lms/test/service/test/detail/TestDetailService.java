package com.goott5.lms.test.service.test.detail;

import com.goott5.lms.test.domain.test.detail.LearnerInfoVO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import jakarta.validation.Valid;
import java.util.List;

public interface TestDetailService {

  // 특정 시험 상세 정보 가져오기
  TestRegisterDTO getTestDetail(int testId);

  // 수강생들 정보 가져오기
  List<LearnerInfoVO> getTestLearners(int testId);

  // 시험 수정
  void modifyTestDetail(@Valid TestRegisterDTO testDetailDTO, int testId);

  // 시험 삭제
  void removeTestDetail(int testId);
}
