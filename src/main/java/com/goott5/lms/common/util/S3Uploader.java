package com.goott5.lms.common.util;


import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Configuration
@Component
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;
    @Value("${cloud.aws.region.static}")
    private String region;

    private S3Client s3Client;


    @PostConstruct
    public void initializeS3(){
        // s3Client 빌드
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    //파일 업로드
    public String uploadFile(String dirName, InputStream inputStream, String originalFileName) throws IOException {

        String ext = originalFileName.substring(originalFileName.lastIndexOf(".")+1);

        //파일 확장자(ext)검사
        if(ext.equalsIgnoreCase("exe")){
//            log.info("실행 파일은 서버 업로드 불가합니다:{}",originalFileName);
            return "exe";
        }

        String uuid = UUID.randomUUID().toString();
        String uploadFileName = dirName + "/" + uuid + "_" +originalFileName;

        File tempFile = convert(inputStream, originalFileName);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(uploadFileName)
                        .acl("public-read")
                        .build(),
                Paths.get(tempFile.getAbsolutePath()));

        tempFile.delete();
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(uploadFileName)).toString();

    }

    // 파일 다운로드(일단 보류)
    public Path downloadFile(String key,String fileDir) throws IOException {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key) //  대상 파일의 key(s3 버켓-객체-파일 클릭-속성-키 에서 확인 가능) (ex. "upload/homework/ddd.jpg")
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        //파일 저장
        return Files.write(Paths.get(fileDir),objectBytes.asByteArray());
        // 실패....(이건 일부 컨트롤러에서 존재하니 삭제하실때 공유 부탁드립니다~!)
    }

    //파일 삭제(이제 가능)
    public void deleteFile(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
//        log.info("delete : {}",  s3Client.deleteObject(builder -> builder.bucket(bucket).key(key)));
//          log.info("key:{}",key); // 성공
//          log.info("파일 서버 삭제 성공:{}",key);
    }

    private File convert(InputStream inputStream, String fileName) throws IOException {
        // 운영체제의 임시 디렉토리 경로에 파일 생성?
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(inputStream.readAllBytes());
        }
        return file;
    }


}
