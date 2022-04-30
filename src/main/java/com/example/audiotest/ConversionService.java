package com.example.audiotest;


import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sun.audio.AudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConversionService {
        //확장자 변환 !!

//    public void mp3ToWav (MultipartFile multipartFile){
//        try {
//
//            byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());
//            bos.write(bytes, 0, 1024000);
//            bos.close();
//            AudioSystem audioSystem = new AudioFile();
//
//
//        } catch (IOException e){
//            log.error(e.getMessage());
//        }
//
//    }


    public void wavToMp3 (){

    }



//        public void run() {
//            try {
//                writer = new NewWaveWriter(44100);
//
//                byte[]buffer = new byte[256];
//                int res = 0;
//                while((res = m_audioInputStream.read(buffer)) > 0) {
//                    writer.write(buffer, 0, res);
//                }
//            } catch (IOException e) {
//                System.out.println("Error: " + e.getMessage());
//            }
//        }
//
//    public byte[]getResult() throws IOException {
//        return writer.getByteBuffer();
//    }
//
//        File f = new File(exportFileName+".tmp");
//        File f2 = new File(exportFileName);
//        long l = f.length();
//        FileInputStream fi = new FileInputStream(f);
//        AudioInputStream ai = new AudioInputStream(fi,mainFormat,l/4);
//    AudioSystem.write(ai, Type.WAVE, f2);
//    fi.close();
//    f.delete();
}
