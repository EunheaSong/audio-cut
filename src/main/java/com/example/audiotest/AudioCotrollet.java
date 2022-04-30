package com.example.audiotest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AudioCotrollet {

    private final AmazonS3 amazonS3;
    private final AudioCutService audioCutService;
    private final AudioPreviewRepository audioPreviewRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //자른 오디오 파일 저장 경로
    private String path = "src/main/resources/static/";


    //오디오 파일 S3에 업로드 !!!
    @PostMapping("/upload")
    public void fileUpload (@RequestPart MultipartFile multipartFile) throws IOException {
        String orifileName = multipartFile.getOriginalFilename();
        String fileName = "audio" + "/" + UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(orifileName);

    }

    //오디오 파일 내 로컬에 잘라서 저장 !!
    @PostMapping("/cut/copy")
    public void filecopy (@RequestPart MultipartFile multipartFile) throws IOException {
        AudioPreview audioPreview = new AudioPreview();
        audioCutService.audioCut(multipartFile, audioPreview, path);
//        System.out.println("파일 복사를 시작합니다.");
//        long startTime = System.currentTimeMillis();
////        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
//        FileOutputStream fos = new FileOutputStream("test.mp3");
//        BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//        byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
//        bos.write(bytes, 0, 1024000);
//        bos.close();
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("파일 복사가 종료되었습니다.");
//        System.out.println("소요시간 : " + ((endTime - startTime) / 1000 + "초"));
    }

    //내 로컬에 저장된 오디오파일을 S3로 업로드 !!
    @PostMapping("/copyAudio/upload")
    public String cutupload () throws IOException{
//        String fileCut = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        String test = "test.mp3";
        FileInputStream fis = new FileInputStream(test);
        BufferedInputStream bis = new BufferedInputStream(fis);
        amazonS3.putObject(new PutObjectRequest(bucket, test, bis, null) //메타 데이터를 넣어주지 않으면 로직 실행시 로그에 경고가 찍힌다.
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3.getUrl(bucket, test).toString();

    }

    //내 로컬에 있는 오디오 삭제
    @DeleteMapping("/remove")
    public void audioRemove () throws IOException {
        Path filePath = Paths.get("C:\\Users\\happy\\IdeaProjects\\audiotest\\test.mp3");
        Files.delete(filePath);
    }

    //내 로컬로 오디오 잘라서 저장하고, S3로 업로드 !!
    @PostMapping("/cut/copy/upload")
    public String cutCopyS3Upload (@RequestPart MultipartFile multipartFile) throws IOException {
        AudioPreview audioPreview = new AudioPreview();
        audioCutService.audioCut(multipartFile, audioPreview, path);

        audioCutService.copyAudioUpload(bucket, audioPreview, multipartFile, path);

        Path filePath = Paths.get(path + audioPreview.getOriName()); //로컬에 남은 오디오 삭제.
        Files.delete(filePath);

        return audioPreview.getS3Name();

    }

    @PostMapping("/test")
    public void test (@RequestPart MultipartFile multipartFile) throws IOException {

        AudioPreview audioPreview = new AudioPreview();

        String localFile = UUID.randomUUID() + "."
                + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());

        String originalFile = UUID.randomUUID() + "."
                + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());

        audioPreview.setOriName(originalFile);
        File file = aa(multipartFile,path,localFile);

        copyAudio(file, path + originalFile, 1, 60);

        audioCutService.copyAudioUpload(bucket, audioPreview, multipartFile, path);

        audioCutService.removeFile(path, localFile);
        audioCutService.removeFile(path, originalFile);

        System.out.println("변환 완료!");

        audioPreviewRepository.save(audioPreview);

    }

    public static File aa (MultipartFile multipartFile, String path, String localFile) throws IOException, UnsupportedAudioFileException {
        File file = new File(path + localFile);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(multipartFile.getBytes());
        bos.close();

        return file;
    }
    //destinationFileName => 편집후 저장할 파일명. 여기에 확장자를 지정해서 적으면 그 확장자로 저장이 되었음! .mp3이렇게.
    public static void copyAudio(File file, String destinationFileName, int startSecond, int secondsToCopy) {

        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);

            AudioFormat format = fileFormat.getFormat();

            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);

            int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();
            inputStream.skip(startSecond * bytesPerSecond);
            long framesOfAudioToCopy = secondsToCopy * (int) format.getFrameRate();
            AudioInputStream shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);

            inputStream.close();
            shortenedStream.close();

        } catch (IOException e){
            log.error(e.getMessage());
        } catch (NullPointerException e) {
            log.error(e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }


    }
}


