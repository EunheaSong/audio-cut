package com.example.audiotest;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class Test {
    //정해진 시간 단위로 오디오 자르기 .. ㅠㅠ
    public static void main(String[]args) {

        copyAudio("가져올 파일 경로,이름",
                "새로 만들 파일 경로,이름", 1, 60);
    }
    public static void copyAudio(String sourceFileName, String destinationFileName, int startSecond, int secondsToCopy) {
//        AudioInputStream inputStream = null;
//        AudioInputStream shortenedStream = null;
        try {
            File file = new File(sourceFileName);

            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);

            AudioFormat format = fileFormat.getFormat();

            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);

            int bytesPerSecond = format.getFrameSize() * (int)format.getFrameRate();
            inputStream.skip(startSecond * bytesPerSecond);
            long framesOfAudioToCopy = secondsToCopy * (int)format.getFrameRate();
            AudioInputStream shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);

            inputStream.close();
            shortenedStream.close();

        } catch (Exception e) {
            System.out.println(e);
        }
//        finally {
//            if (inputStream != null) try {
//                inputStream.close(); }
//            catch (Exception e) {
//                System.out.println(e);
//            }
//            if (shortenedStream != null) try { shortenedStream.close(); } catch (Exception e) {
//                System.out.println(e);
//            }
//        }
    }
}
