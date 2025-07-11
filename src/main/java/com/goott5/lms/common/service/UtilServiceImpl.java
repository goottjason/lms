package com.goott5.lms.common.service;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UtilServiceImpl implements UtilService {

    private final UtilMapper utilMapper;


    @Override
    public int insertService(FileDTO fileDTO) {

        int result = 0;

        // is Image 구분해서 파일 저장
        int separatorInt = fileDTO.getOriginalName().lastIndexOf(".");

        // 확장자
        String ext = (separatorInt != -1)?fileDTO.getOriginalName().substring(separatorInt +1):"";

        // 확장자 "exe" 걸러내기
        if(ext.equals("exe")){
//            log.info("exe는 db 저장 불가합니다:{}",fileDTO.getOriginalName());
            return 0; //file insert mapper 하기 전
        }

        String[] extArr = {"jpg", "jpeg", "png", "gif", "bmp", "tiff", "svg", "webp", "heic"};

        fileDTO.setIsImage(false);

        for (String s : extArr) {
            if(s.equals(ext)){
                fileDTO.setIsImage(true);
                break;
            }
        }

        try {
            result = utilMapper.insertFile(fileDTO);
        } catch (Exception e) {
//            log.info("예외발생:{}",e.getMessage());
        }


        return result;
    }

    @Override
    public List<FileSelectDTO> selectFileList(String tableName, int tableId) {

        return utilMapper.selectFileFrom(tableName,tableId);
    }

    @Override
    public FileSelectDTO selectFileById(int id) {
        return utilMapper.selectFileById(id);
    }

    @Override
    public int deleteFileById(int id) {
        return utilMapper.deleteFileById(id);
    }
}
