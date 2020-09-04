package com.paladin.vod.service;

import com.paladin.framework.exception.BusinessException;
import com.paladin.vod.mapper.UploadFileMapper;
import com.paladin.vod.model.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author TontoZhou
 * @since 2020/8/3
 */
@Slf4j
@Service
public class VideoService {

    @Value("${vod.upload.folder}")
    private String targetFolder;

    @Value("${vod.transcode.folder}")
    private String transcodeFolder;

    @Value("${vod.ffmpeg}")
    private String ffmpeg;

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @PostConstruct
    private void initialize() {
        if (!targetFolder.endsWith("/")) {
            targetFolder += "/";
        }

        if (!transcodeFolder.endsWith("/")) {
            transcodeFolder += "/";
        }

        // 创建目录
        Path root = Paths.get(transcodeFolder);
        try {
            Files.createDirectories(root);
            log.info("转码视频存放目录：" + transcodeFolder);
        } catch (Exception e) {
            log.error("创建转码视频存放目录异常[" + transcodeFolder + "]", e);
        }

        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        UploadFile uploadFile = queue.take();
                        transcode(uploadFile);
                    } catch (InterruptedException e) {
                        log.error("视频转换线程异常", e);
                    }
                }
            }
        };

        thread.setDaemon(true);
        thread.setName("transcode");

        thread.start();
    }

    private LinkedBlockingQueue<UploadFile> queue = new LinkedBlockingQueue<>();

    public void put2transcode(UploadFile uploadFile) {
        queue.offer(uploadFile);
    }

    private void transcode(UploadFile uploadFile) {

        String id = uploadFile.getId();
        int status = uploadFileMapper.getTranscodeStatus(id);
        if (status != UploadFile.TRANSCODE_STATUS_NONE) {
            log.warn("视频[ID:" + id + "]已经转化过");
            return;
        }

        log.info("开始转换视频[ID:" + id + "]");

        String videoPath = targetFolder + uploadFile.getServerRelativePath() + id + uploadFile.getSuffix();
        try {
            if (Files.exists(Paths.get(videoPath))) {

//                获取视频相关信息，这里我们不去判断视频原编码等问题，统一直接转换为视频h264音频aac格式，转码后视频将大大压缩
//                File file = path.toFile();
//                Encoder encoder = new Encoder();
//                MultimediaInfo mi = encoder.getInfo(file);
//
//                String ac = mi.getAudio().getDecoder();
//                String vc = mi.getVideo().getDecoder();

                String outPath = transcodeFolder + id + ".mp4";
                Path out = Paths.get(outPath);
                if (Files.exists(out)) {
                    // 存在文件则进行删除
                    Files.delete(out);
                }

                List<String> command = new ArrayList<>();

                command.add(ffmpeg);
                command.add("-i");
                command.add(videoPath);
                command.add("-c:a");
                command.add("libfdk_aac");
                command.add("-c:v");
                command.add("libx264");
                command.add(outPath);

                runCommand(command);

                if (Files.exists(out)) {
                    uploadFileMapper.updateTranscodeStatus(id, UploadFile.TRANSCODE_STATUS_SUCCESS);
                    log.info("转换视频[" + outPath + "]成功");

                } else {
                    uploadFileMapper.updateTranscodeStatus(id, UploadFile.TRANSCODE_STATUS_ERROR);
                    log.info("转换视频[" + outPath + "]失败");
                }

            } else {
                throw new BusinessException("文件[" + videoPath + "]不存在");
            }
        } catch (Exception e) {
            log.error("转换视频[ID:" + id + "]编码异常", e);
            uploadFileMapper.updateTranscodeStatus(id, UploadFile.TRANSCODE_STATUS_ERROR);
        }
    }

    private int runCommand(List<String> command) throws Exception {
        ProcessBuilder pbuilder = new ProcessBuilder(command);
        Process process = pbuilder.start();
        // 通过线程读取程序输出内容，防止阻塞
        new ReadInputStreamThread(process.getInputStream()).start();
        new ReadInputStreamThread(process.getErrorStream()).start();
        return process.waitFor();
    }

    private static class ReadInputStreamThread extends Thread {

        private InputStream inputStream;

        private ReadInputStreamThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            InputStreamReader inputStreamReader = null;
            BufferedReader br = null;
            try {
                inputStreamReader = new InputStreamReader(
                        inputStream);
                br = new BufferedReader(inputStreamReader);
                String line = null;
                while ((line = br.readLine()) != null) {
                    log.info(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    br.close();
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


//    public static void main(String[] args) throws EncoderException {
//
//        String path = "e:/x.mp4";
//
//        File file = Paths.get(path).toFile();
//        Encoder encoder = new Encoder();
//        MultimediaInfo mi = encoder.getInfo(file);
//
//        String ac = mi.getAudio().getDecoder();
//        String vc = mi.getVideo().getDecoder();
//
//        System.out.println(ac);
//        System.out.println(vc);
//
//    }

}
