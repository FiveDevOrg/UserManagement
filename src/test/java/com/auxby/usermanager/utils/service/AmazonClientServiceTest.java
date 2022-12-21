package com.auxby.usermanager.utils.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmazonClientServiceTest {
    @InjectMocks
    private AmazonClientService amazonClientService;
    @Mock
    private AmazonS3 amazonS3;

    @Test
    @SneakyThrows
    void uploadAvatar() {
        when(amazonS3.putObject(any()))
                .thenReturn(mock(PutObjectResult.class));

        var mockFile = new MockMultipartFile("test", "testFile", MediaType.MULTIPART_FORM_DATA.getType(), "Test".getBytes());
        var result = amazonClientService.uploadAvatar(mockFile, "test-uuid");
        assertNotNull(result);
    }
}