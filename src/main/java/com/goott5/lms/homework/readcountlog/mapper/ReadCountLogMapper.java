package com.goott5.lms.homework.readcountlog.mapper;

import com.goott5.lms.homework.readcountlog.domain.ReadCountLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReadCountLogMapper {
  // (사용자 userId가 테이블 tableName의 게시물tableId의 상세페이지에 readDate 날에 접근했을 때)

  // 기존 기록 확인
  @Select("select count(*) from read_count_log where table_name = #{tableName} and table_id = #{tableId} and user_id = #{userId}")
  int checkReadCountLog(String tableName, int tableId, int userId);

  //------------------------------------------------------------------

  // 1-1 . (기록이 0일 경우) 조회수 테이블 insert
  @Insert("insert into read_count_log (user_id,table_name,table_id,read_date) values (#{userId}, #{tableName}, #{tableId}, now())")
  int insertReadCountLog(ReadCountLog readCountLog);

  // 1-2. (기록이 0일 경우) 해당 게시글 테이블의 read_count 업데이트(read_count + 1) (각자의 매퍼에서 사용)

  //--------------------------------------------------------------------

  // 2. (기록이 1일 경우) now()-readDate가 1인지 확인
  @Select("SELECT (DATEDIFF(NOW(), read_date) < 1) \n"
      + "FROM read_count_log \n"
      + "WHERE user_id = #{userId} \n"
      + "AND table_name = #{tableName} \n"
      + "AND table_id = #{tableId}")
  int checkReadCountLogByDate(ReadCountLog readCountLog);

  // 2-1. (now()-readDate < 1일 경우) (기존 기록 확인 후)그냥 return

  // 2-2.(now()-readDate > 1) readCount테이블의 readDate now()로 수정
  @Update("update read_count_log set read_date = now() where user_id = #{userId} and table_name = #{tableName} and table_id = #{tableId}")
  int updateReadCountLogByDate(ReadCountLog readCountLog);

  // 2-3. (now()-readDate > 1) 해당 게시글 테이블의 read_count 업데이트(read_count + 1) (각자의 매퍼에서 사용)

}
