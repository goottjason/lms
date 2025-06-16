package com.goott5.lms.common.mapper;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UtilMapper {

    // 파일 (db)저장
    int insertFile(FileDTO fileDTO);

    // 게시글이 저장됨과 동시에 게시글의 아이디 반환(파일 저장용에 쓰일 것)
    @Select("select LAST_INSERT_ID()")
    int selectLastIdFromAll();

    //파일 테이블에서 파일 리스트 출력
    List<FileSelectDTO> selectFileFrom(String tableName, int tableId);

}
