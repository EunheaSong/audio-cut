package com.example.audiotest;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class AudioCotrollet {

    private final AmazonS3 amazonS3;
    private final AudioCutService audioCutService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //오디오 파일 S3에 업로드 !!!
    @PostMapping("/upload")
    public void fileUpload (@RequestPart MultipartFile multipartFile) throws IOException {
        String orifileName = multipartFile.getOriginalFilename();
        String fileName = "audio" + "/" + UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

        String a = audioCutService.audioUpload(bucket, fileName, multipartFile, fileCut);

    }

    //오디오 파일 내 로컬에 잘라서 저장 !!
    @PostMapping("/cut/copy")
    public void filecopy (@RequestPart MultipartFile multipartFile) throws IOException {

        System.out.println("파일 복사를 시작합니다.");
        long startTime = System.currentTimeMillis();
//        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream("test.mp3");
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
        bos.write(bytes, 0, 1024000);
        bos.close();

        long endTime = System.currentTimeMillis();
        System.out.println("파일 복사가 종료되었습니다.");
        System.out.println("소요시간 : " + ((endTime - startTime) / 1000 + "초"));
    }

    //내 로컬에 저장된 오디오파일을 S3로 업로드 !!
    @PostMapping("/copyAudio/upload")
    public String cutupload () throws IOException{
//        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        String test = "test.mp3";
        FileInputStream fis = new FileInputStream(test);
        BufferedInputStream bis = new BufferedInputStream(fis);
        amazonS3.putObject(new PutObjectRequest(bucket, test, bis, null)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3.getUrl(bucket, test).toString();

    }

    //내 로컬로 오디오 잘라서 저장하고, S3로 업로드 !!
//    @PostMapping("/cut/copy/upload")
//    public String cutCopyS3Upload (@RequestPart MultipartFile multipartFile){
//
//    }

    //내 로컬에 있는 오디오 삭제
    @DeleteMapping("/remove")
    public void audioRemove () throws IOException {
        Path filePath = Paths.get("C:\\Users\\happy\\IdeaProjects\\audiotest\\test.mp3");
        Files.delete(filePath);
    }

}


