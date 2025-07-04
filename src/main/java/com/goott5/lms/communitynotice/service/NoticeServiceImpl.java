package com.goott5.lms.communitynotice.service;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.ReadCountLogMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.communitynotice.domain.NoticeDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingRequestDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingResponseDTO;
import com.goott5.lms.communitynotice.mapper.NoticeMapper;
import com.goott5.lms.user.domain.UserVO;
import com.goott5.lms.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

  private final NoticeMapper noticeMapper;
  private final UtilService utilService;
  private final S3Uploader s3Uploader;
  private final ReadCountLogMapper readCountLogMapper;
  private final UserMapper userMapper; // UserMapper는 isAdmin에서만 필요할 경우 유지, 아니면 제거 가능

  private static final String TABLE_NAME = "community_notice";

  @Transactional
  @Override
  public int registerNotice(NoticeDTO noticeDTO, List<MultipartFile> files) throws IOException {
    if (noticeDTO.getIsFixed() != null && noticeDTO.getIsFixed()) {
      if (noticeMapper.countPinnedNotices() >= 5) {
        return -1;
      }
    }
    noticeMapper.insertNotice(noticeDTO);
    uploadAndSaveFiles(files, noticeDTO.getId());
    return 1;
  }

  @Override
  public NoticePagingResponseDTO<NoticeDTO> getNoticeList(NoticePagingRequestDTO pagingRequestDTO) {
    List<NoticeDTO> pinnedList = noticeMapper.selectAllPinnedNotices();
    List<NoticeDTO> dtoList = noticeMapper.selectNoticeList(pagingRequestDTO);
    int totalCount = noticeMapper.getNoticeCount(pagingRequestDTO);
    return new NoticePagingResponseDTO<>(pagingRequestDTO, dtoList, totalCount, pinnedList);
  }

  @Override
  public NoticeDTO getNotice(int id) {
    return noticeMapper.getNotice(id);
  }

  // 👇 새로운 조회수 로직 적용 (UserVO 파라미터 사용)
  @Override
  @Transactional
  public void increaseViews(int id, UserVO loginUser) {
    if (loginUser == null) return; // 비로그인 시 조회수 증가 안함

    ReadCountLog readCountLog = ReadCountLog.builder()
        .tableName(TABLE_NAME)
        .tableId(id)
        .userId(loginUser.getId())
        .build();

    int checkReadCount = readCountLogMapper.checkReadCountLog(readCountLog.getTableName(), readCountLog.getTableId(), readCountLog.getUserId());

    if (checkReadCount == 0) { // 첫 방문
      readCountLogMapper.insertReadCountLog(readCountLog);
      noticeMapper.incrementReadCount(id);
      log.info("첫 방문. NoticeId: {}, UserId: {}. 조회수 증가.", id, loginUser.getId());
    } else { // 재방문
      int dateNum = readCountLogMapper.checkReadCountLogByDate(readCountLog);
      if (dateNum == 0) { // 하루 경과 후 방문
        readCountLogMapper.updateReadCountLogByDate(readCountLog);
        noticeMapper.incrementReadCount(id);
        log.info("재방문 (하루 경과). NoticeId: {}, UserId: {}. 조회수 증가.", id, loginUser.getId());
      } else { // 하루 이내 재방문
        log.info("재방문 (하루 이내). NoticeId: {}, UserId: {}. 조회수 증가하지 않음.", id, loginUser.getId());
      }
    }
  }

  @Transactional
  @Override
  public int modifyNotice(NoticeDTO noticeDTO, List<MultipartFile> addFiles, List<Integer> deleteFileNos) throws IOException {
    if (noticeDTO.getIsFixed() != null && noticeDTO.getIsFixed()) {
      NoticeDTO originalPost = noticeMapper.getNotice(noticeDTO.getId());
      if (originalPost != null && !originalPost.getIsFixed()) {
        if (noticeMapper.countPinnedNotices() >= 5) {
          return -1;
        }
      }
    }
    noticeMapper.updateNotice(noticeDTO);
    if (deleteFileNos != null && !deleteFileNos.isEmpty()) {
      for (int fileId : deleteFileNos) {
        FileSelectDTO fileToDelete = utilService.selectFileById(fileId);
        if (fileToDelete != null) {
          s3Uploader.deleteFile(fileToDelete.getNewName());
          utilService.deleteFileById(fileId);
        }
      }
    }
    uploadAndSaveFiles(addFiles, noticeDTO.getId());
    return 1;
  }

  @Transactional
  @Override
  public void removeNotice(int id) {
    List<FileSelectDTO> filesToDelete = utilService.selectFileList(TABLE_NAME, id);
    if (filesToDelete != null && !filesToDelete.isEmpty()) {
      for (FileSelectDTO file : filesToDelete) {
        s3Uploader.deleteFile(file.getNewName());
        utilService.deleteFileById(file.getId());
      }
    }
    noticeMapper.softDeleteNotice(id);
  }

  @Override
  public int getPinnedCount() {
    return noticeMapper.countPinnedNotices();
  }

  // 👇 isAdmin 메소드도 UserVO를 받아서 처리
  @Override
  public boolean isAdmin(UserVO loginUser) {
    return loginUser != null && "ADMINISTRATOR".equals(loginUser.getType());
  }

  private void uploadAndSaveFiles(List<MultipartFile> files, int noticeId) throws IOException {
    if (files != null && !files.isEmpty()) {
      for (MultipartFile file : files) {
        if (file == null || file.isEmpty()) continue;
        String originalName = file.getOriginalFilename();
        String newName = createNewName(originalName);
        String s3Path = s3Uploader.uploadFile(TABLE_NAME, file.getInputStream(), newName);
        FileDTO fileDTO = FileDTO.builder()
            .tableName(TABLE_NAME)
            .tableId(noticeId)
            .originalName(originalName)
            .newName(newName)
            .path(s3Path)
            .size((int) file.getSize())
            .build();
        utilService.insertService(fileDTO);
      }
    }
  }

  private String createNewName(String originalName) {
    String uuid = UUID.randomUUID().toString();
    String ext = "";
    if (originalName != null && originalName.contains(".")) {
      ext = originalName.substring(originalName.lastIndexOf("."));
    }
    return uuid + ext;
  }
}