package com.goott5.lms.test.service.test.register;

import com.goott5.lms.test.domain.pagination.ResponseVO;
import com.goott5.lms.test.domain.test.list.TestListDTO;
import com.goott5.lms.test.domain.test.register.dto.TestRegisterDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface TestRegisterService {

  // 시험 리스트 가져오기
  ResponseVO<TestListDTO> getTestList(String courseName, int currentPageNo);

  // 시험 등록
  void createTest(@Valid TestRegisterDTO testRegisterDTO, HttpSession session);

  // 과정 ID 가져오기
  Integer getCourseId(String courseName);
}
