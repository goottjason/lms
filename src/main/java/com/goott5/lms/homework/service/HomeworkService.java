package com.goott5.lms.homework.service;


import com.goott5.lms.homework.domain.*;

import com.goott5.lms.homework.readcountlog.domain.ReadCountLog;
import java.util.List;
import java.util.Map;

public interface HomeworkService {

  //과제 반환
  PagingResponseDTO<HomeworkDTO> serviceList(HomeworkRequestDTO homeworkRequestDTO,String type);
  
  //과제 제출 반환
  PagingResponseDTO<HomeworkSubmissionDTO> pagingSubmissionDTO(int homeworkId,PagingRequestDTO pagingRequest);

  //과제 제출을 위한 아이디->로그인 아이디 반환
  String selectUserIdForSubmission(int id);

  //관리자용 과제 반환
  PagingResponseDTO<HomeworkDTO> ServiceAdminList(HomeworkRequestDTO homeworkRequestDTO);

  
  //강사, 교육생용 select 메뉴 선택
  List<String> selectCourseMenu(String loginId,String type);


  //관리자의 진행상황(Boolean,tinyint)에 따라 과정명 반환
  List<String> selectBoxCourseNameForAdmin(Boolean isInProgress);

  //-----------상세----------------------------

  // 과제 아이디-> 과제 상세 페이지
  HomeworkDTO selectHomeworkDTOById(int id);

  // int id-> loginId 출력
  String selectLoginId(int id);

  //과제 제출 + 과제 평가
  Map<HomeworkSubmissionDTO, HomeworkEvalDTO> selectSubmissionEval(int submissionId);

  //------------등록--------------------

  // 과제 등록 -> 매퍼에서 성공 시 해당 homeworkDTO의 id를 반환
  int insertHomework(HomeworkDTO homeworkDTO);

  // 과제 등록을 위한 instructorId, course_id 반환
  Map<String,Integer> selectIdCourse(String loginId);

  //------------- 수정----------------

  // 해당 과제의 작성자가 로그인한 아이디의 강사와 일치하는지 확인
  int selectIsInstructorId(String loginId, int homeworkId);

  // 수정
  int updateHomework(HomeworkModifyDTO homeworkModifyDTO);

  //----------- 삭제 ----------------

  // 해당 아이디의 과제 삭제
  int deleteHomeworkById(int id);

  // 조회수 업데이트(homework)
  boolean updateReadCount(ReadCountLog readCountLog);


}
