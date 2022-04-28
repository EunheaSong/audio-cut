package com.example.audiotest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AudioCutService {

    private final AmazonS3 amazonS3;

    //파일 바로 S3에 업로드
    public String audioUpload(String bucket, String fileName, MultipartFile multipartFile, String filecut) {
        String a = "";
        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            a = amazonS3.getUrl(bucket, fileName).toString();

            AudioFile audioFile = new AudioFile();
            audioFile.setOriName(fileName);
            audioFile.setS3Name(a);
            audioFile.setAa(filecut);

            AudioPreview audioPreview = new AudioPreview();
            audioPreview.setOriName(fileName);
            audioPreview.setS3Name(a);
            //재생시간 넣어주기.
//            audioPreview.setPlayTime(new SimpleDateFormat("mm:ss:SSS")).format(new Date(1024000));
            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (IOException e) {
            return "gg";
        }
    }

    //파일 자르고 로컬에 저장
    public void audioCut(MultipartFile multipartFile, AudioPreview audioPreview){
        //stream을 사용할 때는 무조건 예외처리를 해줘야한다. Why?? =>
        //throws 와 try catch 는 뭐가 다를까 ?? =>
        try {
            System.out.println("파일 복사를 시작합니다.");

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());

            String fileCutName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(fileCutName + ".mp3");
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
            bos.write(bytes, 0, 1024000);
            bos.close();

            audioPreview.setOriName(fileCutName+".mp3");

            //재생시간 넣어주기.
//            audioPreview.setPlayTime(new SimpleDateFormat("mm:ss:SSS")).format(new Date(1024000));

        } catch (IOException e){
            log.error(e.getMessage());
        } catch (NullPointerException e){
            log.error(e.getMessage());
        }
    }

    //로컬에 저장된 파일 s3로 업로드
    public void copyAudioUpload(String fileCutName, String bucket, AudioPreview audioPreview){
        try {
            FileInputStream fis = new FileInputStream(fileCutName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            String fileCutNameS3 = "audioPreview" + "/" + fileCutName;

            amazonS3.putObject(new PutObjectRequest(bucket, fileCutNameS3, bis, null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            audioPreview.setS3Name(amazonS3.getUrl(bucket, fileCutNameS3).toString());

//            Files.delete(fileCutName);
        } catch (IOException e){
            log.error(e.getMessage());
        } catch (NullPointerException e){
            log.error(e.getMessage());
        }
    }




}
