package com.goott5.lms.participation.mapper;

import com.goott5.lms.participation.domain.ParticipationReasonDTO;
import com.goott5.lms.participation.domain.ParticipationReasonVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 출결 사유(휴가 등) 관련 DB 매퍼
 */
@Mapper
public interface ParticipationReasonMapper {

  // 사유서 등록
  void insertParticipationReason(ParticipationReasonDTO dto);

  // participationId로 사유서 조회
  ParticipationReasonVO selectReasonByParticipationId(
      @Param("participationId") Integer participationId);

  // 사유서 id로 조회
  ParticipationReasonVO selectReasonById(@Param("id") Integer id);

  // 하드 딜리트 (완전 삭제) - 휴가용
  void deleteParticipationReason(@Param("id") Integer id);
}
