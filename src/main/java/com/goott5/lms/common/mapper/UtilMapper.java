package com.goott5.lms.common.mapper;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultType;
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

    // 파일 삭제를 위한 데이터 조회
    @Select("select id, original_name, new_name, path, is_image, size from file where id = #{id}")
    @ResultType( FileSelectDTO.class)
    FileSelectDTO selectFileById(int id);

    // 파일 db 삭제
    @Delete("delete from file where id = #{id}")
    int deleteFileById(int id);

}
