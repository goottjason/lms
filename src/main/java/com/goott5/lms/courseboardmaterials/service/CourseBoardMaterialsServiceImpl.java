package com.goott5.lms.courseboardmaterials.service;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsFlatDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsVO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;
import com.goott5.lms.courseboardmaterials.mapper.CourseBoardMaterialsMapper;
import com.goott5.lms.user.domain.UserVO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseBoardMaterialsServiceImpl implements CourseBoardMaterialsService {

  private final CourseBoardMaterialsMapper courseBoardMaterialsMapper;
  private final ReadCountLogMapper readCountLogMapper;
  private final UtilService utilService;
  private final S3Uploader s3Uploader;

  @Override
  @Transactional
  public int insertCourseBoardMaterials(CourseBoardMaterialsDTO courseBoardMaterialsDTO) {

    // '고정'으로 설정하려는 경우에만 개수 체크 로직을 수행합니다.
    if (courseBoardMaterialsDTO.getIsFixed()) {
      // 현재 고정된 글의 총 개수를 DB에서 조회합니다.
      int fixedCount = courseBoardMaterialsMapper.countFixedPosts(
          (long) courseBoardMaterialsDTO.getCourseId());
      // 조회된 고정글이 5개 이상이면, 등록을 막고 실패(-1)를 반환합니다.
      if (fixedCount >= 5) {
//        log.warn("고정글은 5개를 초과할 수 없습니다. (현재 {}개)", fixedCount);
        return -1; // -1을 반환하여 등록 실패를 알림
      }
    }

    if (courseBoardMaterialsDTO.getCourseId() == 0) {

      Integer courseId = courseBoardMaterialsMapper.selectCourseId(
          courseBoardMaterialsDTO.getCourseName());

      if (courseId == null) {
        throw new RuntimeException("해당하는 강의 ID를 찾을수 없습니다.");
      }

      courseBoardMaterialsDTO.setCourseId(courseId);
    }

//    log.info("courseBoardMaterialsDTO:{}", courseBoardMaterialsDTO);


    courseBoardMaterialsMapper.insertCourseBoardMaterials(courseBoardMaterialsDTO);

    return courseBoardMaterialsDTO.getId();
  }

  @Override
  public CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> getListWithSearch(
      CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO) {

    // 고정글 목록을 조회
    List<CourseBoardMaterialsPageDTO> pinnedList = courseBoardMaterialsMapper.selectAllFixedPosts(courseBoardMaterialsPagingRequestDTO);

    final String keyword = courseBoardMaterialsPagingRequestDTO.getKeyword();

    // 검색어가 존재하고 비어있지 않을 경우에만 고정글 리스트를 필터링
    if (keyword != null && !keyword.trim().isEmpty()) {
      final String type = courseBoardMaterialsPagingRequestDTO.getType();

      // Java Stream API를 사용하여 필터링 수행
      pinnedList = pinnedList.stream()
          .filter(post -> {
            // 검색 타입이 지정되지 않았다면 제목과 작성자 모두에서 검색
            boolean contentMatch = type.contains("c") && post.getContent() != null && post.getContent().contains(keyword);

            if (type == null || type.trim().isEmpty()) {
              boolean titleMatch = post.getTitle() != null && post.getTitle().contains(keyword);
              boolean writerMatch = post.getWriterName() != null && post.getWriterName().contains(keyword);
              return titleMatch || writerMatch || contentMatch;
            }

            // 검색 타입이 지정되었다면 해당 타입에 맞게 검색
            boolean titleMatch = type.contains("t") && post.getTitle() != null && post.getTitle().contains(keyword);
            boolean writerMatch = type.contains("w") && post.getWriterName() != null && post.getWriterName().contains(keyword);


            return titleMatch || writerMatch || contentMatch;
          })
          .collect(java.util.stream.Collectors.toList());
    }

    // 일반글 목록을 조회
    List<CourseBoardMaterialsVO> regularPostVOs = courseBoardMaterialsMapper.selectListWithSearch(courseBoardMaterialsPagingRequestDTO);

    List<CourseBoardMaterialsPageDTO> regularPostDTOs = new ArrayList<>();
    for (CourseBoardMaterialsVO vo : regularPostVOs) {
      CourseBoardMaterialsPageDTO dto = CourseBoardMaterialsPageDTO.builder()
          .id(vo.getId())
          .title(vo.getTitle())
          .content(vo.getContent())
          .courseId(vo.getCourseId())
          .courseName(vo.getCourseName())
          .writerName(vo.getWriterName())
          .readCount(vo.getReadCount())
          .isFixed(vo.getIsFixed())
          .createdAt(vo.getCreatedAt())
          .updatedAt(vo.getUpdatedAt())
          .deletedAt(vo.getDeletedAt())
          .isAttached(vo.getIsAttached())
          .build();
      regularPostDTOs.add(dto);
    }

    int totalCount = courseBoardMaterialsMapper.selectTotalCountWithSearch(courseBoardMaterialsPagingRequestDTO);


    // 최종 결과를 ResponseDTO에 담아 반환합니다.
    return CourseBoardMaterialsPagingResponseDTO.<CourseBoardMaterialsPageDTO>allInfo()
        .courseBoardMaterialsPagingRequestDTO(courseBoardMaterialsPagingRequestDTO)
        .pinnedList(pinnedList)         // << 필터링이 적용된 고정글 목록
        .dtoList(regularPostDTOs)
        .total(totalCount)
        .build();
  }

  @Override
  @Transactional
  public CourseBoardMaterialsDetailInfo getCourseBoardMaterialsDetail(int id) {

    // Mapper의 새로운 메소드를 호출하여 게시글+작성자 정보 조회
    CourseBoardMaterialsFlatDTO flatDTO = courseBoardMaterialsMapper.selectCourseBoardMaterialsDetailFlat(id);

    if (flatDTO == null) {
      return null; // 게시글이 없으면 null 반환
    }

    // Mapper의 새로운 메소드를 호출하여 첨부파일 목록 조회
    List<FileSelectDTO> attachments = courseBoardMaterialsMapper.selectAttachmentsByBoardId(id);

    // 조회된 두 종류의 데이터를 조합하여 최종 DetailInfo 객체를 직접 생성
    UserVO user = UserVO.builder()
        .id(flatDTO.getWriterId())
        .fullName(flatDTO.getWriterName())
        .type(flatDTO.getWriterType())
        .build();

    CourseBoardMaterialsDetailInfo courseBoardMaterialsDetailInfo = CourseBoardMaterialsDetailInfo.builder()
        .id(flatDTO.getId())
        .courseId(flatDTO.getCourseId())
        .courseName(flatDTO.getCourseName())
        .title(flatDTO.getTitle())
        .content(flatDTO.getContent())
        .readCount(flatDTO.getReadCount())
        .isFixed(flatDTO.isFixed())
        .createdAt(flatDTO.getCreatedAt())
        .updatedAt(flatDTO.getUpdatedAt())
        .writerId(flatDTO.getWriterId())
        .writerName(flatDTO.getWriterName())
        .writerType(flatDTO.getWriterType())
        .user(user)
        .attachments(attachments)
        .isAttached(attachments != null && !attachments.isEmpty())
        .build();

    return courseBoardMaterialsDetailInfo;
  }

  @Override
  @Transactional
  public boolean updateReadCount(ReadCountLog readCountLog) {
    /**
     * 조회수 증가
     * 사용자 userId가 테이블 tableName의 게시물 tableId의 상세페이지에 readDate 날에 접근했을 때
    **/
    String tableName = readCountLog.getTableName();
    int tableId = readCountLog.getTableId();
    int userId = readCountLog.getUserId();

    int checkReadCount = readCountLogMapper.checkReadCountLog(tableName,tableId,userId);

    if(checkReadCount == 0){
      // 처음 방문
      int insertNum = readCountLogMapper.insertReadCountLog(readCountLog);
      if(insertNum == 1){
        int updateNum = courseBoardMaterialsMapper.updateReadCount(tableId);
        if(updateNum == 1){
//          log.info("처음 사용자: 조회수 insert && update 성공");
          return true;
        }
      }
    } else {
      // 두 번째 방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if(dateNum == 1){
        // 하루 이내 방문
//        log.info("이후 사용자: 조회수 증가 x");
        return true;
      }else{
        // 하루 이후 방문
        int updateReadDate = readCountLogMapper.updateReadCountLogByDate(readCountLog);
        if(updateReadDate == 1){
          int updateReadCount = courseBoardMaterialsMapper.updateReadCount(tableId);
          if(updateReadCount == 1){
//            log.info("이후 사용자: 조회수 증가 o");
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  @Transactional
  public int updateCourseBoardMaterials(CourseBoardMaterialsDTO courseBoardMaterialsDTO) {
    // '고정'으로 설정하려는 경우에만 개수 체크 로직을 수행합니다.
    if (courseBoardMaterialsDTO.getIsFixed()) {
      // 이 게시글의 원래 상태를 조회하여, 원래는 고정글이 아니었는지 확인합니다.
      // (이미 고정된 글을 다시 저장하는 경우는 개수 제한에 걸리지 않도록 하기 위함)
      CourseBoardMaterialsDetailInfo originalPost = this.getCourseBoardMaterialsDetail(courseBoardMaterialsDTO.getId());

      // 원래 고정글이 아니었던 글을 새로 고정하려는 경우
      if (originalPost != null && !originalPost.getIsFixed()) {
        // 현재 고정된 글의 총 개수를 DB에서 조회합니다.
        int fixedCount = courseBoardMaterialsMapper.countFixedPosts(
            (long) courseBoardMaterialsDTO.getCourseId());

        // 조회된 고정글이 5개 이상이면, 수정을 막고 실패(0)를 반환합니다.
        if (fixedCount >= 5) {
//          log.warn("고정글은 5개를 초과할 수 없습니다. (현재 {}개)", fixedCount);
          return 0; // 0을 반환하여 업데이트 실패를 알림
        }
      }
    }

    int result = courseBoardMaterialsMapper.updateCourseBoardMaterials(courseBoardMaterialsDTO);
    return result;
  }

  @Override
  @Transactional
  public void deleteCourseBoardMaterials(int courseBoardMaterialsId) {
    // 삭제할 게시글에 연결된 모든 첨부파일 목록을 DB에서 조회
    //    UtilService의 메소드를 사용합니다.
    List<FileSelectDTO> filesToDelete = utilService.selectFileList("course_notice", courseBoardMaterialsId);

    // 첨부파일이 존재하면 S3와 DB에서 모두 삭제
    if (filesToDelete != null && !filesToDelete.isEmpty()) {
      for (FileSelectDTO file : filesToDelete) {
        // S3 저장소에서 물리적인 파일 삭제
        // S3 key는 '폴더명/저장된파일명' 형태여야 합니다.
        String s3Key = "course-materials/" + file.getNewName();
        s3Uploader.deleteFile(s3Key);

        // DB의 file 테이블에서 해당 파일 정보 삭제
        utilService.deleteFileById(file.getId());
      }
    }

    // 마지막으로 게시글을 soft delete 처리
    courseBoardMaterialsMapper.softDeleteById(courseBoardMaterialsId);
  }

  @Override
  public int countFixedPostsByCourseId(Long courseId) {
    if (courseId == null) {
      return 0; // courseId가 없으면 고정글도 0개로 간주
    }
    // Mapper의 countFixedPosts를 호출합니다.
    return courseBoardMaterialsMapper.countFixedPosts(courseId);
  }

}
