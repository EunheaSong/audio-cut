package com.example.audiotest;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class AudioCotrollet {

    private final AmazonS3 amazonS3;
    private final AudioCutService audioCutService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @PostMapping("/upload")
    public void fileUpload (@RequestPart MultipartFile multipartFile) throws IOException {
        String orifileName = multipartFile.getOriginalFilename();
        String fileName = "audio" + "/" + UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

        String a = audioCutService.ff(bucket, fileName, multipartFile, fileCut);


        audioCutService.cut(bucket,multipartFile,a, fileCut);


//        try(FileInputStream fis = new FileInputStream(multipartFile);
    }

    @PostMapping("/copy")
    public void filecopy (@RequestPart MultipartFile multipartFile) throws IOException {

        System.out.println("파일 복사를 시작합니다.");
        long startTime = System.currentTimeMillis();

        FileOutputStream fos = new FileOutputStream("test.mp3");
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
        fos.write(bytes, 0, 1024000);
        bos.close();

        long endTime = System.currentTimeMillis();
        System.out.println("파일 복사가 종료되었습니다.");
        System.out.println("소요시간 : " + ((endTime - startTime) / 1000 + "초"));
    }

}


