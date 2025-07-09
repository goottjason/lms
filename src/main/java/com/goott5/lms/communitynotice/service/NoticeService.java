package com.goott5.lms.communitynotice.service;

import com.goott5.lms.communitynotice.domain.NoticeDTO;
import com.goott5.lms.communitynotice.domain.NoticeIdDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingRequestDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingResponseDTO;
import com.goott5.lms.user.domain.UserVO; // UserVO import
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface NoticeService {

  NoticeIdDTO registerNotice(NoticeDTO noticeDTO, List<MultipartFile> files) throws IOException;

  NoticePagingResponseDTO<NoticeDTO> getNoticeList(NoticePagingRequestDTO pagingRequestDTO);

  NoticeDTO getNotice(int id);

  // 👇 조회수 증가 메소드에 UserVO 파라미터를 받도록 수정
  void increaseViews(int id, UserVO loginUser);

  int modifyNotice(NoticeDTO noticeDTO, List<MultipartFile> addFiles, List<Integer> deleteFileNos) throws IOException;

  void removeNotice(int id);

  int getPinnedCount();

  // 👇 isAdmin 메소드도 UserVO 파라미터를 받도록 수정
  boolean isAdmin(UserVO loginUser);
}