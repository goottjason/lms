package com.goott5.lms.courseboarddebate.util;

import com.goott5.lms.courseboarddebate.mapper.CourseBoardDebateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotPostScheduler {

  private final CourseBoardDebateMapper debateMapper;

  // 매일 자정(00시 00분 00초)에 실행되도록 cron
  @Scheduled(cron = "0 0 0 * * *")
  public void expireHotPosts() {
    log.info("만료된 고정 글 해제 작업을 시작합니다...");
    int updatedRows = debateMapper.expireHotPosts();
    if (updatedRows > 0) {
      log.info("{}개의 고정 글이 해제되었습니다.", updatedRows);
    }
  }
}
