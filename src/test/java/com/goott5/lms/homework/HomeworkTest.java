package com.goott5.lms.homework;
import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.homework.domain.*;
import com.goott5.lms.homework.mapper.HomeworkMapper;

import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import java.time.LocalDateTime;
import java.util.*;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;




@SpringBootTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HomeworkTest {


  @Autowired(required = true)
  HomeworkMapper homeworkMapper;

  @Autowired(required = true)
  UtilMapper utilMapper;

  @Autowired(required = true)
  ReadCountLogMapper readCountLogMapper;


  @Test
  void contextLoads() {

    HomeworkRequestDTO homeworkRequestDTO = HomeworkRequestDTO.builder()
        .loginId("ahn1234")
        .nameForLt("자바 1회차 ㅇㅇ")
        .order("asc")
        .sortBy("title")
        .build();

    homeworkRequestDTO.setPagingRequest(PagingRequestDTO.builder()
        .pageNo(1)
        .pageSize(5)
        .build());

    List<HomeworkDTO> homeworkDTOList = homeworkMapper.searchBySelectForTeacher(homeworkRequestDTO);

    //mapper에서 받아온 homeworkDTOList(pageSize만큼)를 PagingResponseDTO의 리스트에 넣어 반환

    //total 게시글 수
    int total = homeworkMapper.searchCountForTeacher(homeworkRequestDTO);

    //pagingResonseDTO 형성(builder 활용)
    PagingResponseDTO<HomeworkDTO> pagingResponseDTO = PagingResponseDTO.<HomeworkDTO>allInfo()
        .pagingRequestDTO(homeworkRequestDTO.getPagingRequest())
        .dtoList(homeworkDTOList)
        .total(total)
        .build();

    log.info(pagingResponseDTO.toString());
    //  log.info("homeworkDTOList={}", homeworkDTOList); //성공

  }


  @Test
  public void HomeworkSubmissionTest() {

    List<HomeworkSubmissionDTO> submissionDTOS = homeworkMapper.selectSubmissionById(1,PagingRequestDTO.builder().pageNo(1).pageSize(5).build());

    if(submissionDTOS != null){

    log.info("submissionDTOS:{}",submissionDTOS);
    } else{
      log.info("null");
    }

  }

  @Test
  public void menuSelectTest() {
    if(homeworkMapper.selectCourseMenuForTeacher("ahn1234") != null){
      log.info("menuSelectTest:{}",homeworkMapper.selectCourseMenuForTeacher("ahn1234"));
    } else {
      log.info("null");
    }
  }

  @Test
  public void selectMenuForAdmin(){

//   boolean truStr = Boolean.parseBoolean("true");
//   boolean falStr = Boolean.parseBoolean("false");
   //boolean truOrFalse =  Boolean.parseBoolean("");
    Boolean truStr = true;
    Boolean falStr = false;

   log.info("truStr 과정명 조회:{}",homeworkMapper.selectBoxCourseNameForAdmin(truStr));
   log.info("falStr 과정명 조회:{}",homeworkMapper.selectBoxCourseNameForAdmin(falStr));
   log.info("falStr 과정명 조회:{}",homeworkMapper.selectBoxCourseNameForAdmin(null));
   //log.info("truOrFalse 과정명 조회:{}",homeworkMapper.selectBoxCourseNameForAdmin(truOrFalse));

  }

  @Test
  @Transactional
  public void dummyTest(){

    for(int i=0;i<5;i++){
      HomeworkSubmissionDTO hs = HomeworkSubmissionDTO.builder()
          .homeworkId(9)
          .title("dummy" + i)
          .content(i+"번째 과제 제출")
          .learnerId(4)
          .build();
//      homeworkMapper.insertHomeworkSubmission(hs);
//      7, "dummy" + i, i+"번째 과제 제출",4
      if(homeworkMapper.insertHomeworkSubmission(hs) !=1){
        log.info("insertHomeworkSubmission:{}",hs);
      }
    }


  }

  @Test
  public void selectDetailTest(){
    HomeworkDTO homeworkDTO = homeworkMapper.selectHomeworkDTOById(1);

    if(homeworkDTO != null){
     log.info("homeworkDTO 상세 테스트:{}",homeworkDTO);
    }

  }

  @Test
  public void selectSubmissionWithDetailTest(){

    Map<HomeworkSubmissionDTO, HomeworkEvalDTO> testMap = new HashMap<>();

    HomeworkSubmissionDTO submission = homeworkMapper.selectSubmissionBySubmissionId(28);
    Optional<HomeworkEvalDTO> eval = Optional.ofNullable(homeworkMapper.selectEvalById(28));

    if(submission != null) {
      testMap.put(submission,eval.orElse(null));
    }

    log.info("testMap:{}",testMap);

  }

  @Test
  @Transactional
  public void insertHomeworkTest(){

    HomeworkDTO homeworkDTO = HomeworkDTO.builder()
            .title("queen 과제22222")
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(40))
            .content("dummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummydummy")
            .courseId(6)
            .instructorId(6)
            .build();

    if(homeworkMapper.insertHomework(homeworkDTO) == 1) {
      log.info("성공");
    } else {
      log.info("실패");
    }

  }

  @Test
  public void testSelectForInsert(){

    List<Map<String, Integer>> resultList = homeworkMapper.selectForInsertId("queen");

    if(!resultList.isEmpty()) {
      log.info("resultList:{}",resultList.get(0));
    } else {
      log.info("없어용:{}", resultList);
    }

  }

  @Test
  @Transactional
  public void testInsertFile(){

    FileDTO fileDTO = FileDTO.builder()
            .originalName("hamburger.jpg")
            .newName("0df7d1df-99a1-415c-b771-ffec75f1f561_hamburger.jpg")
            .path("https://s3uploads-bucketpyj.s3.ap-northeast-2.amazonaws.com/upload/0df7d1df-99a1-415c-b771-ffec75f1f561_hamburger.jpg")
            .isImage(true)
            .size((int) 237.8 * 1024)
            .tableName("homework")
            .tableId(15)
            .build();

    if(utilMapper.insertFile(fileDTO)== 1){
      log.info("fileDTO insert 성공:{}",fileDTO);
    }else{
      log.info("실패했습니다.");
    };
  }

  @Test
  public void testSelectIsHomeworkInstructor(){

    String loginId = "queen";
    int homeworkId = 27;

   int result = homeworkMapper.selectIsInstructorId(loginId, homeworkId);

   if(result == 1){
     log.info("select 성공");
   }

  }

  @Test
  @Transactional(rollbackFor = Exception.class)
  public void testSelectModifyHomework(){

    HomeworkModifyDTO homeworkModifyDTO = HomeworkModifyDTO.builder()
        .title("하이")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(40))
        .content("언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니언니")
        .id(27)
        .build();

   int result =  homeworkMapper.updateHomework(homeworkModifyDTO);
   if(result == 1){
     log.info("update success");
   } else {
     log.info("update fail");
   }

  }

  @Test
  public void selectFile(){

    FileSelectDTO fileSelectDTO = utilMapper.selectFileById(15);

    if(fileSelectDTO != null){
      log.info("selectFile:{}",fileSelectDTO);
    }else{

      log.info("존재하지 않음");
    }
  }

  @Test
  @Transactional(rollbackFor = Exception.class)
  public void testDeleteFile(){
    int result = utilMapper.deleteFileById(15);
    if(result == 1){
      log.info("delete success");
    } else {
      log.info("delete fail");
    }
  }

  @Test
  @Transactional(rollbackFor = Exception.class)
  public void testDeleteHomework(){
    int result = homeworkMapper.deleteHomeworkById(34);
    if(result == 1){
      log.info("delete success");
    } else {
      log.info("delete fail");
    }
  }

  @Test
  @Transactional(rollbackFor = Exception.class)
  public void testReadCount(){
    // 사용자 A가 처음으로 상세페이지 B에 접근했을 때
    int readCount = readCountLogMapper.checkReadCountLog("homework",34,6);

    log.info("readCount:{}",readCount);

    ReadCountLog readCountLog = ReadCountLog.builder()
        .userId(6)
        .tableName("homework")
        .tableId(34)
        .readDate(new Date())
        .build();

    log.info("readCountLog:{}",readCountLog);

    int insertReadCount = readCountLogMapper.insertReadCountLog(readCountLog);

    if(insertReadCount == 1){
      log.info("readCount insert success");
    }else{
      log.info("readCount insert fail");
      return;
    }

    int updateTableRead = homeworkMapper.updateReadCount(34);

    if(updateTableRead == 1){
      log.info("readCount update success");
    }else{
      log.info("readCount update fail");
    }

  }

  @Test
  @Transactional(rollbackFor = Exception.class)
  public void testUpdateHomeworkReadCount(){
    // 사용자 A가 두번째로 상세페이지 B에 접근했을 때(24시간 이내)
    // 사용자 A가 두번째로 상세페이지 B에 접근했을 때(24시간 이후)
    int readCount = readCountLogMapper.checkReadCountLog("homework",34,6);

    log.info("readCount= 1?:{}",readCount);

    ReadCountLog readCountLog = ReadCountLog.builder()
        .tableName("homework")
        .tableId(34)
        .userId(6)
        .build();

    int date = readCountLogMapper.checkReadCountLogByDate(readCountLog);

    if(date == 1){
      log.info("조회수 업데이트 x");
    } else {
      int updateReadCount = readCountLogMapper.updateReadCountLogByDate(readCountLog);

      if(updateReadCount == 1){
        log.info("readCount update success");
        int result = homeworkMapper.updateReadCount(34);
        if(result == 1){
          log.info("table update success");
        }else{
          log.info("table update fail");
        }
      }
    }


  }
}
