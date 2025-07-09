package com.goott5.lms.homework.service;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.homework.domain.*;
import com.goott5.lms.homework.mapper.HomeworkMapper;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.homework.util.FileException;
import java.io.IOException;
import java.util.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {

  private final HomeworkMapper homeworkMapper;
  private final UtilMapper utilMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final S3Uploader s3Uploader;
  private final UtilService utilService;


  @Override
  public String courseNameById(int id) {
    return homeworkMapper.courseName(id);
  }

  @Override
  public int courseIdById(int id) {
    return homeworkMapper.courseId(id);
  }

  @Override
  public int instructorIdByCourseId(int courseId) {
    return homeworkMapper.instructorIdByCourseId(courseId);
  }

  @Override
  public PagingResponseDTO<HomeworkDTO> serviceList(HomeworkRequestDTO homeworkRequestDTO,
      String type) {
    List<HomeworkDTO> homeworkDTOList = new ArrayList<>();
    int total = 0;

    if (type.equals("LEARNER")) {
      //mapper에서 받아온 homeworkDTOList(pageSize만큼)를 PagingResponseDTO의 리스트에 넣어 반환
      homeworkDTOList = homeworkMapper.searchBySelectForLearner(homeworkRequestDTO);
      total = homeworkMapper.searchCountForLearner(homeworkRequestDTO);
    } else if (type.equals("INSTRUCTOR")) {
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
  public PagingResponseDTO<HomeworkSubmissionForListDTO> pagingSubmissionDTO(int homeworkId,
      PagingRequestDTO pagingRequest) {

    List<HomeworkSubmissionForListDTO> submissionDTOS = homeworkMapper.selectSubmissionById(homeworkId,
        pagingRequest); //기능 추가: dto에 필드 추가해 불러오기(250707)

    //total 게시글 수
    int total = homeworkMapper.totalSubmission(homeworkId);

    PagingResponseDTO<HomeworkSubmissionForListDTO> pagingSubmission = PagingResponseDTO
        .<HomeworkSubmissionForListDTO>allInfo()
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
    if (userId != null) {
      return userId;
    }
    return null;
  }

  @Override
  public int selectUserPkForSubmission(int submissionId) {
    Integer userPk = homeworkMapper.selectUserPkForSubmission(submissionId);
    if (userPk != null) {
      return userPk;
    }
    return -1;
  }

  @Override
  public PagingResponseDTO<HomeworkDTO> ServiceAdminList(HomeworkRequestDTO homeworkRequestDTO) {

    List<HomeworkDTO> homeworkDTOList = homeworkMapper.searchBySelectForAdmin(homeworkRequestDTO);
    int total = homeworkMapper.searchCountForAdmin(homeworkRequestDTO);

    if (homeworkDTOList == null || homeworkDTOList.isEmpty()) {
      homeworkDTOList = new ArrayList<>();
    }

    PagingResponseDTO<HomeworkDTO> pagingResponseDTO = PagingResponseDTO.<HomeworkDTO>allInfo()
        .pagingRequestDTO(homeworkRequestDTO.getPagingRequest())
        .dtoList(homeworkDTOList)
        .total(total)
        .build();

    log.info("페이징 결과: 서비스 단 " + pagingResponseDTO.toString());

    return pagingResponseDTO;
  }

  @Override
  public List<String> selectCourseMenu(String loginId, String type) {
    List<String> menuList = new ArrayList<>();

    if (type.equals("INSTRUCTOR")) {
      menuList = homeworkMapper.selectCourseMenuForTeacher(loginId);
    } else if (type.equals("LEARNER")) {
      menuList = homeworkMapper.selectCourseMenuForLearner(loginId);
    }
    return menuList;
  }

  @Override
  public List<String> selectBoxCourseNameForAdmin(Boolean isInProgress) {

    return homeworkMapper.selectBoxCourseNameForAdmin(isInProgress) == null ? new ArrayList<>()
        : homeworkMapper.selectBoxCourseNameForAdmin(isInProgress);
  }

  @Override
  public String homeworkName(int id) {
    return homeworkMapper.selectTitle(id);
  }

  @Override
  public boolean selectSubmissionLearner(int homeworkId, int learnerId) {
    return homeworkMapper.existSubmission(learnerId, homeworkId) == 1;
  }

  @Override
  public HomeworkDTO selectHomeworkDTOById(int id) {
    HomeworkDTO homeworkDTO = homeworkMapper.selectHomeworkDTOById(id);

    if (homeworkDTO != null) {
      return homeworkDTO;
    }

    return null;
  }

  @Override
  public String selectLoginId(int id) {
    String instructorLoginId = homeworkMapper.selectInstructorIdForHomework(id);
    if (instructorLoginId != null) {
      return instructorLoginId;
    }
    return null;
  }

  @Override
  public Map<HomeworkSubmissionDTO, HomeworkEvalDTO> selectSubmissionEval(int submissionId) {

    Map<HomeworkSubmissionDTO, HomeworkEvalDTO> resultMap = new HashMap<>();

    HomeworkSubmissionDTO submission = homeworkMapper.selectSubmissionBySubmissionId(submissionId);

    Optional<HomeworkEvalDTO> eval = Optional.ofNullable(
        homeworkMapper.selectEvalById(submissionId));

    if (submission != null) {
      resultMap.put(submission, eval.orElse(null));
    }

    return resultMap;
  }

  // submission 객체 보내기
  @Override
  public HomeworkSubmissionDTO selectSubmission(int submissionId) {

    return homeworkMapper.selectSubmissionBySubmissionId(submissionId);
  }


  @Override
  @Transactional(rollbackFor = Exception.class)
  public int insertHomework(HomeworkDTO homeworkDTO) {
    int idForHomework = -1;
    if (homeworkMapper.insertHomework(homeworkDTO) == 1) {
      idForHomework = utilMapper.selectLastIdFromAll();
    }
    return idForHomework;
  }


  @Override
  public Map<String, Integer> selectIdCourse(String name, int userId, String type) {
    List<Map<String, Integer>> resultMap = homeworkMapper.selectForInsertId(name, userId, type);
    if (!resultMap.isEmpty()) {
      return resultMap.get(0);
    }
    return null;
  }

  @Override
  public int selectIsInstructorId(String loginId, int homeworkId) {
    int result = homeworkMapper.selectIsInstructorId(loginId, homeworkId);

    return result;
  }

  @Override
  public int selectIsInstructorIdByPk(int userId, int homeworkId) {
    return homeworkMapper.selectIsInstructorIdByPk(userId, homeworkId);
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
  public boolean selectHomeworkSubmissionIdByHomework(int homeworkId) {

    return homeworkMapper.selectHomeworkSubmissionIdByHomework(homeworkId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateReadCount(ReadCountLog readCountLog) {
    // (사용자 userId가 테이블 tableName의 게시물 tableId의 상세페이지에 readDate 날에 접근했을 때)
    String tableName = readCountLog.getTableName();
    int tableId = readCountLog.getTableId();
    int userId = readCountLog.getUserId();

    int checkReadCount = readCountLogMapper.checkReadCountLog(tableName, tableId, userId);

    if (checkReadCount == 0) {
      // 처음 방문
      int insertNum = readCountLogMapper.insertReadCountLog(readCountLog);
      if (insertNum == 1) {
        int updateNum = homeworkMapper.updateReadCount(tableId);
        if (updateNum == 1) {
          log.info("처음 사용자: 조회수 insert && update 성공");
          return true;
        }
      }
    } else {
      // 두 번째 방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if (dateNum == 1) {
        // 하루 이내 방문
        log.info("이후 사용자: 조회수 증가 x");
        return true;
      } else {
        // 하루 이후 방문
        int updateReadDate = readCountLogMapper.updateReadCountLogByDate(readCountLog);
        if (updateReadDate == 1) {
          int updateReadCount = homeworkMapper.updateReadCount(tableId);
          if (updateReadCount == 1) {
            log.info("이후 사용자: 조회수 증가 o");
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public int selectSubmissionIdForLearner(int homeworkId, int learnerId) {
    List<Integer> submissionIdList = homeworkMapper.selectSubmissionIdForLearner(homeworkId, learnerId);

    if( submissionIdList == null || submissionIdList.isEmpty() || submissionIdList.get(0) == null ) {
      return -1;
    }
    return submissionIdList.get(0);
  }

  @Override
  public HomeworkDTO selectHomeworkDTOBySubmissionId(int submissionId) {
    return homeworkMapper.selectHomeworkDTOBySubmissionId(submissionId) == null ? null
        : homeworkMapper.selectHomeworkDTOBySubmissionId(submissionId);
  }

  // submission readCountLog 업데이트
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateReadCountForSubmission(ReadCountLog readCountLog) {
    // (사용자 userId가 테이블 tableName의 게시물 tableId의 상세페이지에 readDate 날에 접근했을 때)
    String tableName = readCountLog.getTableName();
    int tableId = readCountLog.getTableId();
    int userId = readCountLog.getUserId();

    int checkReadCount = readCountLogMapper.checkReadCountLog(tableName, tableId, userId);

    if (checkReadCount == 0) {
      // 처음 방문
      int insertNum = readCountLogMapper.insertReadCountLog(readCountLog);
      if (insertNum == 1) {
        int updateNum = homeworkMapper.updateReadCountForSubmission(tableId);
        if (updateNum == 1) {
          log.info("처음 submission 사용자: 조회수 insert && update 성공");
          return true;
        }
      }
    } else {
      // 두 번째 방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if (dateNum == 1) {
        // 하루 이내 방문
        log.info("이후 submission 사용자: 조회수 증가 x");
        return true;
      } else {
        // 하루 이후 방문
        int updateReadDate = readCountLogMapper.updateReadCountLogByDate(readCountLog);
        if (updateReadDate == 1) {
          int updateReadCount = homeworkMapper.updateReadCountForSubmission(tableId);
          if (updateReadCount == 1) {
            log.info("이후 submission 사용자: 조회수 증가 o");
            return true;
          }
        }
      }
    }
    return false;


  }

  // eval readCountLog 업데이트
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateReadCountForEval(ReadCountLog readCountLog) {
    // (사용자 userId가 테이블 tableName의 게시물 tableId의 상세페이지에 readDate 날에 접근했을 때)
    String tableName = readCountLog.getTableName();
    int tableId = readCountLog.getTableId();
    int userId = readCountLog.getUserId();

    int checkReadCount = readCountLogMapper.checkReadCountLog(tableName, tableId, userId);

    if (checkReadCount == 0) {
      // 처음 방문
      int insertNum = readCountLogMapper.insertReadCountLog(readCountLog);
      if (insertNum == 1) {
        int updateNum = homeworkMapper.updateReadCountForEval(tableId);
        if (updateNum == 1) {
          log.info("처음 eval 사용자: 조회수 insert && update 성공");
          return true;
        }
      }
    } else {
      // 두 번째 방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if (dateNum == 1) {
        // 하루 이내 방문
        log.info("이후 eval 사용자: 조회수 증가 x");
        return true;
      } else {
        // 하루 이후 방문
        int updateReadDate = readCountLogMapper.updateReadCountLogByDate(readCountLog);
        if (updateReadDate == 1) {
          int updateReadCount = homeworkMapper.updateReadCountForEval(tableId);
          if (updateReadCount == 1) {
            log.info("이후 eval 사용자: 조회수 증가 o");
            return true;
          }
        }
      }
    }
    return false;


  }

  // 파일과 함께 insertHomework
  @Override
  @Transactional(rollbackFor = Exception.class)
  public int insertHomeworkSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO) {
    int idForHomework = -1;
    if (homeworkMapper.insertHomeworkSubmission(homeworkSubmissionDTO) == 1) {
      idForHomework = utilMapper.selectLastIdFromAll();
    }
    return idForHomework;
  }

  @Override
  public boolean canSubmission(int learnerId, int homeworkId) {

    int firstTest = homeworkMapper.isLearnerInCourse(learnerId, homeworkId);
    int secondTest = homeworkMapper.existSubmission(learnerId, homeworkId);

    if (firstTest == 1) {
      //해당 과제의 과정에 속한 학생인지 확인
      // 해당 학생의 해당 과제에 대한 제출기록이 존재 x인지 확인(맞으면 제출 가능)
      return secondTest == 0;
    }

    return false;
  }

  @Override
  public int updateSubmission(HomeworkSubmissionDTO homeworkSubmissionDTO) {
    return homeworkMapper.updateSubmission(homeworkSubmissionDTO);
  }

  @Override
  public boolean deleteSubmissionById(int id) {
    int result = homeworkMapper.deleteSubmissionById(id);
    if (result == 1) {
      return true;
    }
    return false;
  }

  @Override
  public Boolean isInProgressByHomeworkId(int homeworkId) {
    return homeworkMapper.isInProgressByHomeworkId(homeworkId);
  }

  @Override
  public int insertEval(HomeworkEvalDTO homeworkEvalDTO) {
      int idForHomework = -1;
      if (homeworkMapper.insertEval(homeworkEvalDTO) == 1) {
        idForHomework = utilMapper.selectLastIdFromAll();
      }
      return idForHomework;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void insertFileFor(List<MultipartFile> fileList,
      String tableName, int tableId, String dirName) {

    // 파일 저장 실패 시, 기존의 업로드되고 저장된 파일들도 삭제됨
    List<String> deleteList = new ArrayList<>();

    //게시글 등록 후 파일 서버 업로드
    //파일 받기
    if (fileList != null) {
      log.info("eval fileList:{}", fileList);
      for (MultipartFile file : fileList) {
        String uploadPath = "";
        if (file == null || file.isEmpty() || file.getSize() == 0) {

          //롤백
          deleteFileForRollback(deleteList);
          log.info("파일이 누락되었습니다");
         throw new FileException(422,"파일이 누락되었습니다",file);
        }
        //파일 서버에 저장
        try {
          uploadPath = s3Uploader.uploadFile(dirName, file.getInputStream(),
              file.getOriginalFilename());

          if (!StringUtils.hasText(uploadPath)) {

            //롤백
            deleteFileForRollback(deleteList);

            log.info("파일 서버 저장 실패:{}", uploadPath);
            throw new FileException(503,"파일 서버 저장 실패",file);

          }

          String key = uploadPath.substring(uploadPath.indexOf(dirName));
          deleteList.add(key); //delete할 키

          // 파일 db에 저장
          FileDTO fileDTO = FileDTO.builder()
              .originalName(file.getOriginalFilename())
              .newName(uploadPath.substring(uploadPath.lastIndexOf("/") + 1))
              .path(uploadPath)
              .size((int) file.getSize())
              .tableName(tableName)
              .tableId(tableId)
              .build();
          int dbNum = utilService.insertService(fileDTO);

          if (dbNum != 1) {

            //롤백
            deleteFileForRollback(deleteList);

            log.info("db 파일 저장 실패");
            throw new FileException(409,"db 파일 저장 실패",file);

          }
        } catch (IOException e) {

          //롤백
          deleteFileForRollback(deleteList);

          log.info("db 파일 서버 저장 실패");

          throw new FileException(503,"파일 서버 저장 실패",file);

        }
      }
    }
  }

  @Override
  public void deleteFileForRollback(List<String> keyList) {

    for (String key : keyList) {
      try {
        s3Uploader.deleteFile(key);
      } catch (Exception e) {
        log.info("파일 삭제 실패:{}", key, e.getMessage());
      }
    }
  }

  @Override
  public boolean updateEval(HomeworkEvalModifyDTO homeworkEvalModifyDTO) {

    int result = homeworkMapper.updateEval(homeworkEvalModifyDTO);

    if (result == 1) {
      return  true;
    }

    return false;
  }

  //과제 평가 삭제
  @Override
  public boolean deleteEvalById(int id) {
    if (homeworkMapper.deleteEvalById(id) == 1) {
      return true;
    }
    return false;
  }

}
