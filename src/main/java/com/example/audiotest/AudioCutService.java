package com.example.audiotest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AudioCutService {
    private final AmazonS3 amazonS3;

//    public void cut(String bucket, MultipartFile multipartFile ,String fileName, String fileCut) throws IOException {
////        String a = amazonS3.getUrl(bucket, fileName).toString();
//            FileOutputStream fos = new FileOutputStream(fileName);
//            BufferedInputStream bis = new BufferedInputStream(multipartFile.getInputStream());
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//            int i;
//            byte[] bs = new byte[1000000];
//            while ((i = bis.read()) < 2500) {
//                bos.write(i);
//            }
//
//
//    }

    public void cut(String bucket, MultipartFile multipartFile ,String fileName, String fileCut) throws IOException {
        String cutFile = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(Mimetypes.getInstance().getMimetype(cutFile));

//        BufferedInputStream bis = new BufferedInputStream(multipartFile.getInputStream());
        try(FileOutputStream fos = new FileOutputStream(cutFile)){

            byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
            fos.write(bytes,0,1000);


//            byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
//            objectMetadata.setContentLength(bytes.length);
//            ByteArrayInputStream byteArrayIs = new ByteArrayInputStream(bytes);
            new FileInputStream(String.valueOf(fos));
            amazonS3.putObject(new PutObjectRequest(bucket,  cutFile,new FileInputStream(String.valueOf(fos)),  objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        }




    }



    public String ff(String bucket, String fileName, MultipartFile multipartFile, String filecut) {
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

            String url = amazonS3.getUrl(bucket, fileName).toString();
            return url;

        } catch (IOException e) {
            return "gg";
        }
    }
}
