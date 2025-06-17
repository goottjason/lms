package com.goott5.lms.common.util;


import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    public String uploadFile(String dirName, InputStream inputStream, String originalFileName) throws IOException {
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

    public void deleteFile(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    }

    private File convert(InputStream inputStream, String fileName) throws IOException {
        // 운영체제의 임시 디렉토리 경로에 파일 생성?
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(inputStream.readAllBytes());
        } catch (FileNotFoundException e) {

            throw new RuntimeException(e);
        }
        return file;
    }


}
