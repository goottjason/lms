package com.goott5.lms.homework.service;


import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.homework.domain.*;

import com.goott5.lms.common.domain.ReadCountLog;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Select;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface HomeworkService {

  //과정명 반환
  String courseNameById(int id);

  //과정 아이디(homeworkId로 반환)
  int courseIdById(int id);

  //과정 아이디로 강사 아이디 반환
  int instructorIdByCourseId(int courseId);

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

  //과제 번호에 따른 과제명 반환
  String homeworkName(int id);

  //-----------상세----------------------------

  // 과제 아이디-> 과제 상세 페이지
  HomeworkDTO selectHomeworkDTOById(int id);

  // int id-> loginId 출력
  String selectLoginId(int id);


  //------------등록--------------------

  // 과제 등록 -> 매퍼에서 성공 시 해당 homeworkDTO의 id를 반환
  int insertHomework(HomeworkDTO homeworkDTO);

  //과제 등록 시, select한 과정이 현재 진행 중인지/앞선 가정을 통과한 과정이 로그인한 강사에게 배정되어 있는지(과제 등록 버튼 막는 용도)
//  Boolean selectIsInProgressAndInSa(String nameForLt,int userId,String userType);

  // 과제 등록을 위한 instructorId, course_id 반환
  Map<String,Integer> selectIdCourse(String name, int userId, String type);

  //------------- 수정----------------

  // 해당 과제의 작성자가 로그인한 아이디의 강사와 일치하는지 확인
  int selectIsInstructorId(String loginId, int homeworkId);

  // 수정
  int updateHomework(HomeworkModifyDTO homeworkModifyDTO);

  //----------- 삭제 ----------------

  // 해당 아이디의 과제 삭제
  int deleteHomeworkById(int id);

  //해당 과제의 submission이 존재하는 지 확인
  boolean selectHomeworkSubmissionIdByHomework(int homeworkId);

  //-------homework 조회수-------------

  // 조회수 업데이트(homework)
  boolean updateReadCount(ReadCountLog readCountLog);

  //--------- 과제 제출 및 평가 리스트---------------------------------------

  //과제 제출 + 과제 평가
  Map<HomeworkSubmissionDTO, HomeworkEvalDTO> selectSubmissionEval(int submissionId);

  // 그냥 submission만 보내기
  HomeworkSubmissionDTO selectSubmission(int submissionId);

  //제출 아이디로 homeworkDTO 출력
  HomeworkDTO selectHomeworkDTOBySubmissionId(int submissionId);

  //homeowork_submission 조회수
  boolean updateReadCountForSubmission(ReadCountLog readCountLog);

  //homework_eval 조회수
  boolean updateReadCountForEval(ReadCountLog readCountLog);

  //homeworkSubmission insert
  int insertHomeworkSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);

  //homeworkSubmission 등록 시 검사
  boolean canSubmission(int learnerId, int homeworkId);

  //homeworkSubmission update
  int updateSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO);

  //homeworkSubmission delete
  boolean deleteSubmissionById(int id);

  //---------과제 평가 등록----------------------------

  //homeworkEval insert
  int insertEval(HomeworkEvalDTO homeworkEvalDTO);

  //----파일 서버 저장 + db 저장-------------------
  void insertFileFor(List<MultipartFile> fileList,
      String tableName, int tableId, String dirName);

  // 파일 저장 서버 롤백
  void deleteFileForRollback(List<String> keyList);

  //----------과제 평가 수정----------------------
  boolean updateEval(HomeworkEvalModifyDTO homeworkEvalModifyDTO);

  //----------과제 평가 삭제-----------------------
  boolean deleteEvalById(int id);
}
