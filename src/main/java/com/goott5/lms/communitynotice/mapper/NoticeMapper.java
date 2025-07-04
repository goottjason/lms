package com.goott5.lms.communitynotice.mapper;

import com.goott5.lms.communitynotice.domain.NoticeDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingRequestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

  // 게시글 등록
  int insertNotice(NoticeDTO notice);

  // 게시글 목록 조회 (ServiceImpl에서 호출하는 이름과 일치시킴)
  List<NoticeDTO> selectNoticeList(NoticePagingRequestDTO pagingRequestDTO);

  // 게시글 총 개수 조회 (ServiceImpl에서 호출하는 이름과 일치시킴)
  int getNoticeCount(NoticePagingRequestDTO pagingRequestDTO);

  // 게시글 상세 조회
  NoticeDTO getNotice(int id);

  // 조회수 업데이트 (ServiceImpl에서 호출하는 이름과 일치시킴)
  void incrementReadCount(int id);

  // 게시글 수정
  int updateNotice(NoticeDTO notice);

  // 게시글 삭제 (ServiceImpl에서 호출하는 이름과 일치시킴)
  void softDeleteNotice(int id);

  // 고정 공지 개수 조회 (ServiceImpl에서 호출하는 이름과 일치시킴)
  int countPinnedNotices();

  // 고정 공지 목록 조회
  List<NoticeDTO> selectAllPinnedNotices();
}