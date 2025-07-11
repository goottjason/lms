package com.goott5.lms.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CreatePOI {

  private final ObjectMapper objectMapper;
  public static String filePath = "C:/downloads";
  public static String[] ext = {".xls", ".xlsx", ".doc", ".docx", ".ppt", ".pptx"};

  public void isExcel(String originalFileName, Map<String, Object> data) {

    //파일 이름 생성
    UUID uuid = UUID.randomUUID();
    String newFileName = uuid.toString() + "_" + originalFileName;
//    log.info("파일 이름 생성:{}", newFileName);

    // 빈 WorkBook 생성
    XSSFWorkbook workBook = new XSSFWorkbook();
//    log.info("workbook 생성:{}", workBook);

    // 빈 sheet 생성
    XSSFSheet sheet = workBook.createSheet("tmp_sheet");
//    log.info("sheet 생성:{}", sheet);

    //sheet를 채우기 위한 데이터 저장
    Map<String, Object> sheetMap = new LinkedHashMap<>();
//    log.info("sheetMap 초기 생성:{}", sheetMap);

    sheetMap.putAll(data);

    //디렉토리 확인 및 생성
    File dir = new File(filePath);
    if (!dir.exists()) {
      boolean isDirCreated = dir.mkdirs();
//      log.info("dir 생성됨:{}", isDirCreated);
    }

    //sheetMap에서 keySet 가져오기 => 조회하면서 sheet에 입력
    Set<String> keySet = sheetMap.keySet();
    int rowNum = 0;
//    log.info("sheetMap의 keySet 생성:{}", keySet);

    // treeMap을 통해 생성된 keySet=> 키값이 오름차순으로 조회됨

    for (String key : keySet) {
      Row row = sheet.createRow(rowNum); //row 초기화
      // 첫번째 셀에 키 넣기
      Cell keyCell = row.createCell(0);
      keyCell.setCellValue(key);

      Object value = sheetMap.get(key);

      if (sheetMap.get(key) instanceof List<?> valueList) {
        //row 값 리스트
        for (int i = 0; i < valueList.size(); i++) {
          if (valueList.get(i) == null || valueList.get(i).toString().isEmpty()) {
            continue;
          }

          Cell valueCell = row.createCell(i + 1);

          if (valueList.get(i) instanceof String) {
            valueCell.setCellValue(valueList.get(i).toString());
          } else if (valueList.get(i) instanceof Number numberValue) {
            valueCell.setCellValue(numberValue.doubleValue());
          } else if (valueList.get(i) instanceof Boolean booleanValue) {
            valueCell.setCellValue(booleanValue);
          } else {
            valueCell.setCellValue(valueList.get(i).toString());
          }
        }
      } else {
        //단일 row값
        Cell valueCell = row.createCell(1);
        if (value != null) {
          valueCell.setCellValue(sheetMap.get(key).toString());
        }
      }
      rowNum++;

    }
//    log.info("sheet list 형성 완료:{}", sheet);
    try (FileOutputStream out = new FileOutputStream(
        new File(filePath, newFileName + ext[1]))){
//      log.info("파일 생성 완료:{}", out);

      try {
        workBook.write(out);
//        out.close();
//        log.info("과정 완료:{}", out, "workBook:{}", workBook);
      } catch (IOException e) {
//        log.info("파일 접근 불가:{}", e.getMessage());
        throw new RuntimeException(e);
      }

    } catch (IOException e) {
//      log.info("파일 찾을 수 x:{}", e.getMessage());
      throw new RuntimeException(e);
    }
  }



  public Map<String, Object> dtoToMap(Object dto) {
    // 파라미터 dto를 map으로 변환(엑셀 전환에 쓰일 맵으로)
    ObjectMapper objectMapper = new ObjectMapper();

    Map map = objectMapper.convertValue(dto, Map.class);

    return map;
  }

}
