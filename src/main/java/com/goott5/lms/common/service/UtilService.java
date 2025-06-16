package com.goott5.lms.common.service;


import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import java.util.List;

public interface UtilService {

    // 파일 저장
    int insertService(FileDTO fileDTO);

    // 파일 select해서 게시판에서 조회
    List<FileSelectDTO> selectFileList(String tableName, int tableId);

}
