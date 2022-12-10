package com.auxby.usermanager.utils.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AmazonClientService {
    @Value("${aws.bucket}")
    private String bucket;
    @Value("${aws.endpoint}")
    private String endpoint;

    private final AmazonS3 amazonS3;

    public String uploadAvatar(MultipartFile avatar, String uuid) throws IOException {
        File file = convertToFile(avatar);
        String fileName = String.format("avatar-%s", uuid);
        String fileUrl = endpoint + "/" + bucket + "/" + fileName;
        uploadFileToS3Bucket(fileName, file);
        file.delete();

        return fileUrl;
    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(convFile)) {
            fileOutputStream.write(multipartFile.getBytes());
        }

        return convFile;
    }

    private void uploadFileToS3Bucket(String fileName, File file) {
        var uploadRequest = new PutObjectRequest(bucket, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(uploadRequest);
    }
}
