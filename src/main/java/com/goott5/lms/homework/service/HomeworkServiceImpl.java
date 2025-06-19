package com.goott5.lms.homework.service;

import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.homework.domain.*;
import com.goott5.lms.homework.mapper.HomeworkMapper;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import java.util.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {
  private final HomeworkMapper homeworkMapper;
  private final UtilMapper utilMapper;
  private final ReadCountLogMapper readCountLogMapper;

  @Override
  public PagingResponseDTO<HomeworkDTO> serviceList(HomeworkRequestDTO homeworkRequestDTO,String type) {
    List<HomeworkDTO> homeworkDTOList = new ArrayList<>();
    int total = 0;

    if(type.equals("LEARNER")){
      //mapper에서 받아온 homeworkDTOList(pageSize만큼)를 PagingResponseDTO의 리스트에 넣어 반환
      homeworkDTOList = homeworkMapper.searchBySelectForLearner(homeworkRequestDTO);
      total = homeworkMapper.searchCountForLearner(homeworkRequestDTO);
    } else if(type.equals("INSTRUCTOR")){
      //선생님일 경우
      homeworkDTOList = homeworkMapper.searchBySelectForTeacher(homeworkRequestDTO);
      //total 게시글 수
      total = homeworkMapper.searchCountForTeacher(homeworkRequestDTO);

    } //추후 관리자도 추가

    //pagingResonseDTO 형성(builder 활용)
    PagingResponseDTO<HomeworkDTO> pagingResponseDTO = PagingResponseDTO.<HomeworkDTO>allInfo()
        .pagingRequestDTO(homeworkRequestDTO.getPagingRequest())
        .dtoList(homeworkDTOList)
        .total(total)
        .build();

    log.info(pagingResponseDTO.toString());

    return pagingResponseDTO;
  }

  @Override
  public PagingResponseDTO<HomeworkSubmissionDTO> pagingSubmissionDTO(int homeworkId,PagingRequestDTO pagingRequest) {

    List<HomeworkSubmissionDTO> submissionDTOS = homeworkMapper.selectSubmissionById(homeworkId,pagingRequest);

    //total 게시글 수
    int total = homeworkMapper.totalSubmission(homeworkId);

    PagingResponseDTO<HomeworkSubmissionDTO> pagingSubmission = PagingResponseDTO
        .<HomeworkSubmissionDTO>allInfo()
        .dtoList(submissionDTOS)
        .pagingRequestDTO(PagingRequestDTO.builder()
            .pageNo(pagingRequest.getPageNo())
            .pageSize(pagingRequest.getPageSize())
            .build())
        .total(total)
        .build();

    return pagingSubmission;
  }

  @Override
  public String selectUserIdForSubmission(int id) {
    String userId = homeworkMapper.selectUserIdForSubmission(id);
    if(userId != null){
      return userId;
    }
    return null;
  }

  @Override
  public PagingResponseDTO<HomeworkDTO> ServiceAdminList(HomeworkRequestDTO homeworkRequestDTO) {

    List<HomeworkDTO> homeworkDTOList = homeworkMapper.searchBySelectForAdmin(homeworkRequestDTO);
    int total = homeworkMapper.searchCountForAdmin(homeworkRequestDTO);

    if(homeworkDTOList == null || homeworkDTOList.isEmpty()){
      homeworkDTOList = new ArrayList<>();
    }


    PagingResponseDTO<HomeworkDTO> pagingResponseDTO = PagingResponseDTO.<HomeworkDTO>allInfo()
        .pagingRequestDTO(homeworkRequestDTO.getPagingRequest())
        .dtoList(homeworkDTOList)
        .total(total)
        .build();

    log.info("페이징 결과: 서비스 단 "+pagingResponseDTO.toString());

    return pagingResponseDTO;
  }

  @Override
  public List<String> selectCourseMenu(String loginId, String type) {
    List<String> menuList = new ArrayList<>();

    if(type.equals("INSTRUCTOR")){
      menuList = homeworkMapper.selectCourseMenuForTeacher(loginId);
    } else if(type.equals("LEARNER")){
      menuList = homeworkMapper.selectCourseMenuForLearner(loginId);
    }
    return menuList;
  }

  @Override
  public List<String> selectBoxCourseNameForAdmin(Boolean isInProgress) {

    return homeworkMapper.selectBoxCourseNameForAdmin(isInProgress) == null ? new ArrayList<>() : homeworkMapper.selectBoxCourseNameForAdmin(isInProgress);
  }

  @Override
  public HomeworkDTO selectHomeworkDTOById(int id) {
    HomeworkDTO homeworkDTO = homeworkMapper.selectHomeworkDTOById(id);

    if(homeworkDTO != null){
      return homeworkDTO;
    }

    return null;
  }

  @Override
  public String selectLoginId(int id) {
    String instructorLoginId = homeworkMapper.selectInstructorIdForHomework(id);
    if(instructorLoginId != null){
      return instructorLoginId;
    }
    return null;
  }

  @Override
  public Map<HomeworkSubmissionDTO, HomeworkEvalDTO> selectSubmissionEval(int submissionId) {

    Map<HomeworkSubmissionDTO,HomeworkEvalDTO> resultMap = new HashMap<>();

    HomeworkSubmissionDTO submission = homeworkMapper.selectSubmissionBySubmissionId(submissionId);

    Optional<HomeworkEvalDTO> eval = Optional.ofNullable(homeworkMapper.selectEvalById(submissionId));

    if(submission != null){
      resultMap.put(submission,eval.orElse(null));
    }

    return resultMap;
  }

  @Override
  @Transactional
  public int insertHomework(HomeworkDTO homeworkDTO) {
    int idForHomework = -1;
    if(homeworkMapper.insertHomework(homeworkDTO) == 1){
      idForHomework = utilMapper.selectLastIdFromAll();
    }
    return idForHomework;
  }

//  @Override
//  @Transactional
//  public Boolean selectIsInProgressAndInSa(String nameForLt,int userId,String userType) {
//
//    Boolean first = homeworkMapper.selectIsInProgress(nameForLt);
//    if(first != null && first){
//      Boolean second = homeworkMapper.selectIsInProgressForSelectBox(nameForLt,userId,userType);
//      if(second != null && second){
//        return true;
//      }
//    }
//
//    return false;
//  }

  @Override
  public Map<String, Integer> selectIdCourse(String name, int userId, String type) {
    List<Map<String,Integer>> resultMap = homeworkMapper.selectForInsertId(name,userId,type);
    if(!resultMap.isEmpty()){
      return resultMap.get(0);
    }
    return null;
  }

  @Override
  public int selectIsInstructorId(String loginId, int homeworkId) {
    int result = homeworkMapper.selectIsInstructorId(loginId,homeworkId);

    return result;
  }

  @Override
  public int updateHomework(HomeworkModifyDTO homeworkModifyDTO) {
    int result = homeworkMapper.updateHomework(homeworkModifyDTO);

    return result;
  }

  @Override
  public int deleteHomeworkById(int id) {

    return homeworkMapper.deleteHomeworkById(id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateReadCount(ReadCountLog readCountLog) {
    // (사용자 userId가 테이블 tableName의 게시물 tableId의 상세페이지에 readDate 날에 접근했을 때)
    String tableName = readCountLog.getTableName();
    int tableId = readCountLog.getTableId();
    int userId = readCountLog.getUserId();

    int checkReadCount = readCountLogMapper.checkReadCountLog(tableName,tableId,userId);

    if(checkReadCount == 0){
      // 처음 방문
      int insertNum = readCountLogMapper.insertReadCountLog(readCountLog);
      if(insertNum == 1){
        int updateNum = homeworkMapper.updateReadCount(tableId);
         if(updateNum == 1){
           log.info("처음 사용자: 조회수 insert && update 성공");
           return true;
         }
      }
    } else {
      // 두 번째 방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if(dateNum == 1){
        // 하루 이내 방문
        log.info("이후 사용자: 조회수 증가 x");
        return true;
      }else{
        // 하루 이후 방문
        int updateReadDate = readCountLogMapper.updateReadCountLogByDate(readCountLog);
        if(updateReadDate == 1){
          int updateReadCount = homeworkMapper.updateReadCount(tableId);
          if(updateReadCount == 1){
            log.info("이후 사용자: 조회수 증가 o");
            return true;
          }
        }
      }
    }
    return false;
  }


}
