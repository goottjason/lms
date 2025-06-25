package com.goott5.lms.common.controller;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.mapper.UtilMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


@Controller
@RequiredArgsConstructor
@Slf4j
public class FileController {

  @Value("${cloud.aws.s3.bucketName}")
  private String bucket;
  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;
  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;
  @Value("${cloud.aws.region.static}")
  private String region;

  private S3Client s3Client;
  private final UtilMapper utilMapper;

  // S3Uploader처럼 S3Client 객체를 초기화합니다.
  @PostConstruct
  public void initializeS3(){
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @GetMapping("/download")
  public ResponseEntity<Resource> downloadFile(@RequestParam("fileId") int fileId) throws IOException {

    // fileId로 DB에서 파일 정보 조회
    FileSelectDTO fileInfo = utilMapper.selectFileById(fileId);
    if (fileInfo == null) {
      return ResponseEntity.notFound().build();
    }

    // 파일의 전체 URL에서 S3 경로(key) 추출
    log.info("1. DB에서 조회한 파일 정보: {}", fileInfo.toString());

    String fileUrl = fileInfo.getPath();
    log.info("2. DB에서 가져온 파일 전체 경로(URL): {}", fileUrl);

    URL url = new URL(fileInfo.getPath());
    String encodedS3Key = url.getPath().substring(1); // URL에서 인코딩된 키 추출
    log.info("3. URL에서 추출한 S3 Key (인코딩된 상태): {}", encodedS3Key);

    // 추출한 키를 UTF-8 방식으로 디코딩하여 원래의 파일 경로로 변환합니다.
    String decodedS3Key = URLDecoder.decode(encodedS3Key, StandardCharsets.UTF_8);
    log.info("4. 디코딩된 최종 S3 Key: {}", decodedS3Key);

    //  S3에서 해당 파일 객체 요청
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(decodedS3Key)
        .build();

    ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);

    // 다운로드 시 사용할 원본 파일 이름으로 헤더 설정 (한글 깨짐 방지)
    String encodedOriginalName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedOriginalName + "\"");

    // 파일 데이터(Resource)와 헤더를 담아 최종 응답
    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(s3ObjectStream));
  }
}