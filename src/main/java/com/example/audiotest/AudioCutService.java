package com.example.audiotest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class AudioCutService {

    private final AmazonS3 amazonS3;

    //파일 바로 S3에 업로드
    public void audioUpload(String bucket, MultipartFile multipartFile, String filecut) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());

        try {
            String fileName = "audio" + "/" + UUID.randomUUID() + "." + StringUtils.
                    getFilenameExtension(multipartFile.getOriginalFilename());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            String S3url = amazonS3.getUrl(bucket, fileName).toString();

            AudioFile audioFile = new AudioFile();
            audioFile.setOriName(fileName);
            audioFile.setS3Name(S3url);

            AudioPreview audioPreview = new AudioPreview();
            audioPreview.setOriName(fileName);
            audioPreview.setS3Name(S3url);
            //재생시간 넣어주기.
//            audioPreview.setPlayTime(new SimpleDateFormat("mm:ss:SSS")).format(new Date(1024000));

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    //파일 자르고 로컬에 저장
    @Transactional
    public void audioCut(MultipartFile multipartFile, AudioPreview audioPreview, String path){
        //stream을 사용할 때는 무조건 예외처리를 해줘야한다. Why?? =>
        //throws 와 try catch 는 뭐가 다를까 ?? =>
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());

            String fileCutName = UUID.randomUUID() + "." +
                    StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            //FileOutputStream을 사용하여 파일을 복사할때 , 따로 경로를 지정해주지 않으면 프로젝트 파일안에 저장된다.
            FileOutputStream fos = new FileOutputStream(path+ fileCutName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            //오디오 파일 시간을 고정되게 자를 순 없을까 ??
            //동일한 바이트 크기로 잘라 주어도 파일마다 재생 시간이 다르다 ㅠ
            byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
            bos.write(bytes, 0, 1024000);
            bos.close();

            audioPreview.setOriName(fileCutName);
            //재생시간 넣어주기.
//            audioPreview.setPlayTime(new SimpleDateFormat("mm:ss:SSS")).format(new Date(1024000));
        } catch (IOException e){
            log.error(e.getMessage());
        } catch (NullPointerException e){
            log.error(e.getMessage());
        }
    }

    //로컬에 저장된 파일 s3로 업로드
    @Transactional
    public void copyAudioUpload(String bucket, AudioPreview audioPreview, MultipartFile multipartFile, String path){

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            FileInputStream fis = new FileInputStream(path + audioPreview.getOriName());
            BufferedInputStream bis = new BufferedInputStream(fis);
            String fileCutNameS3 = "audioPreview" + "/" + audioPreview.getOriName();

            amazonS3.putObject(new PutObjectRequest(bucket, fileCutNameS3, bis, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            audioPreview.setS3Name(amazonS3.getUrl(bucket, fileCutNameS3).toString());

        } catch (IOException e){
            log.error(e.getMessage());
        } catch (NullPointerException e){
            log.error(e.getMessage());
        }
    }

    @Transactional
    public void removeFile (String path, String originalFile) throws IOException {
        Path filePath = Paths.get(path + originalFile); //로컬에 남은 오디오 삭제.
        Files.delete(filePath);
    }

    //재생시간 .. 되는지는 아직 모름 ...
    @Transactional
    public float time (File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = file.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        return durationInSeconds;
    }

}
