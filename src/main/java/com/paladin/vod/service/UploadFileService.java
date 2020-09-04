package com.paladin.vod.service;

import com.paladin.framework.exception.BusinessException;
import com.paladin.framework.service.Condition;
import com.paladin.framework.service.PageResult;
import com.paladin.framework.service.QueryType;
import com.paladin.framework.service.ServiceSupport;
import com.paladin.framework.spring.SpringContainer;
import com.paladin.framework.utils.UUIDUtil;
import com.paladin.framework.utils.convert.DateFormatUtil;
import com.paladin.vod.mapper.UploadFileMapper;
import com.paladin.vod.model.UploadFile;
import com.paladin.vod.service.dto.UploadFileDTO;
import com.paladin.vod.service.dto.UploadFileQuery;
import com.paladin.vod.service.vo.UploadFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author TontoZhou
 * @since 2020/8/4
 */
@Slf4j
@Service
public class UploadFileService extends ServiceSupport<UploadFile> implements SpringContainer, FileUploader.FileUploadListener {


    @Value("${vod.upload.chuck-size:5}")
    private int chunkSize;  // 单位M

    @Value("${vod.upload.folder}")
    private String targetFolder;    // 文件上传存放文件夹

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @Autowired
    private VideoService videoService;

    private Map<String, FileUploader> fileUploaderMap = new ConcurrentHashMap<>();

    // 项目启动后执行
    public boolean afterInitialize() {
        if (!targetFolder.endsWith("/")) {
            targetFolder += "/";
        }

        // 创建目录
        Path root = Paths.get(targetFolder);
        try {
            Files.createDirectories(root);
            log.info("视频存放目录：" + targetFolder);
        } catch (Exception e) {
            log.error("创建视频存放目录异常[" + targetFolder + "]", e);
        }


        // 定时清理任务
        Executors.newSingleThreadScheduledExecutor((runnable) -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("cleanUploader");
            return thread;
        }).scheduleWithFixedDelay(() -> cleanUploader(), 120, 120, TimeUnit.SECONDS);

        // 启动时候检查一次需要转码的视频
        List<UploadFile> list = searchAll(
                new Condition(UploadFile.FIELD_STATUS, QueryType.EQUAL, UploadFile.STATUS_COMPLETED),
                new Condition(UploadFile.FIELD_TRANSCODE_STATUS, QueryType.EQUAL, UploadFile.TRANSCODE_STATUS_NONE)
        );

        for (UploadFile uploadFile : list) {
            videoService.put2transcode(uploadFile);
        }

        return true;
    }

    private String getCurrentUserId() {
        // TODO 加入用户认证后改写该方法
        return "test";
    }

    private static HashSet<String> videoSuffixSet = new HashSet<>();

    static {
        // 原理上这只是容器，是否ffmpeg支持转换需要查看音频、视频具体编码（例如h264等）
        videoSuffixSet.add(".mp4");
        videoSuffixSet.add(".avi");
        videoSuffixSet.add(".wmv");
        videoSuffixSet.add(".mpeg");
        videoSuffixSet.add(".mov");
        videoSuffixSet.add(".rmvb");
        videoSuffixSet.add(".flv");
    }

    /**
     * 创建并保存上传文件
     */
    public UploadFile createUploadFile(UploadFileDTO uploadFileDTO) {
        UploadFile uploadFile = new UploadFile();

        String id = UUIDUtil.createUUID();
        String fileName = uploadFileDTO.getFileName();
        String clientPath = uploadFileDTO.getClientFilePath();
        Long fileSize = uploadFileDTO.getFileSize();
        Date now = new Date();

        uploadFile.setId(id);
        uploadFile.setUserId(getCurrentUserId());
        uploadFile.setFileName(fileName);
        uploadFile.setClientFilePath(clientPath);

        int i = fileName.lastIndexOf(".");
        String suffix = i > 0 ? fileName.substring(i) : "";

        suffix = suffix.toLowerCase();
        if (!videoSuffixSet.contains(suffix)) {
            throw new BusinessException("只支持格式为" + videoSuffixSet.toString() + "的视频上传");
        }

        uploadFile.setSuffix(suffix);

        String relativePath = DateFormatUtil.getThreadSafeFormat("yyyyMMdd").format(now) + "/";
        uploadFile.setServerRelativePath(relativePath);

        uploadFile.setChunkSize(chunkSize);
        uploadFile.setFileSize(fileSize);
        uploadFile.setFinishChunk(0);
        uploadFile.setCreateTime(now);
        uploadFile.setUpdateTime(now);
        uploadFile.setStatus(UploadFile.STATUS_UPLOADING);
        uploadFile.setTranscodeStatus(UploadFile.TRANSCODE_STATUS_NONE);

        save(uploadFile);

        return uploadFile;
    }


    /**
     * 获取或创建上传器
     */
    public FileUploader getOrCreateUploader(String id) {

        FileUploader uploader = fileUploaderMap.get(id);

        if (uploader == null) {
            synchronized (fileUploaderMap) {
                uploader = fileUploaderMap.get(id);
                if (uploader == null) {
                    UploadFile uploadFile = get(id);
                    if (uploadFile == null) {
                        throw new BusinessException("上传的文件不存在");
                    }
                    int status = uploadFile.getStatus();
                    if (status != UploadFile.STATUS_UPLOADING) {
                        switch (status) {
                            case UploadFile.STATUS_COMPLETED:
                                throw new BusinessException("上传文件已经完成");
                            case UploadFile.STATUS_DELETE:
                            case UploadFile.STATUS_DELETED:
                            case UploadFile.STATUS_DELETE_ERROR:
                                throw new BusinessException("上传文件已经被删除，请重新上传");
                            default:
                                throw new BusinessException("上传文件异常，请重新上传");
                        }
                    }
                    uploader = new FileUploader(uploadFile, this, targetFolder);
                    fileUploaderMap.put(id, uploader);
                }
            }
        }

        return uploader;
    }

    /**
     * 上传文件块
     *
     * @param id          上传文件ID
     * @param chunkIndex  块序号
     * @param inputStream 块数据流
     */
    public FileUploader uploadFileChunk(String id, int chunkIndex, InputStream inputStream) {
        FileUploader uploader = getOrCreateUploader(id);
        uploader.uploadFileChunk(chunkIndex, inputStream);
        return uploader;
    }


    /**
     * 清理uploader
     */
    public void cleanUploader() {
        // 10分钟未操作和已经关闭的的uploader将被清理
        long time = System.currentTimeMillis() - 60L * 1000 * 10;
        Iterator<Map.Entry<String, FileUploader>> it = fileUploaderMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, FileUploader> entry = it.next();
            FileUploader uploader = entry.getValue();
            if (uploader.isCompleted() || uploader.getLastUpdateTime() < time) {
                uploader.close();
                it.remove();
            }
        }
    }


    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanUploadFile() {
        log.info("开始清理过期与错误的上传文件");

        try {
            uploadFileMapper.updateExpiredUploadFile();
            List<UploadFile> uploadFiles = uploadFileMapper.findErrorUploadFile();
            int count = 0;

            for (UploadFile uf : uploadFiles) {
                try {
                    String filePath = targetFolder + uf.getServerRelativePath() + uf.getId();
                    Path path = Paths.get(filePath + ".tmp");
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                    path = Paths.get(filePath + uf.getSuffix());
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                    count++;
                    uploadFileMapper.updateStatus(uf.getId(), UploadFile.STATUS_DELETED);
                } catch (Exception e) {
                    uploadFileMapper.updateStatus(uf.getId(), UploadFile.STATUS_DELETE_ERROR);
                }
            }

            log.info("删除了[" + count + "]个临时文件");
        } catch (Exception e) {
            log.error("清理错误上传文件异常", e);
        }
    }


    /**
     * 查找用户上传中的文件
     *
     * @return
     */
    public List<UploadFileVO> findUploadingFile() {
        return searchAll(UploadFileVO.class,
                new Condition(UploadFile.FIELD_STATUS, QueryType.EQUAL, UploadFile.STATUS_UPLOADING),
                new Condition(UploadFile.FIELD_USER_ID, QueryType.EQUAL, getCurrentUserId())
        );
    }

    /**
     * 查找上传完成的文件
     *
     * @param query
     * @return
     */
    public PageResult<UploadFileVO> findCompletedFile(UploadFileQuery query) {
        query.setStatus(UploadFile.STATUS_COMPLETED);
        return searchPage(query, UploadFileVO.class);
    }

    /**
     * 置为删除状态
     *
     * @param id
     */
    public void removeUploadFile(String id) {
        synchronized (fileUploaderMap) {
            FileUploader uploader = fileUploaderMap.remove(id);
            if (uploader != null) {
                uploader.close();
            }
        }
        uploadFileMapper.updateStatus(id, UploadFile.STATUS_DELETE);
    }

    @Override
    public void completedSuccess(FileUploader uploader) {
        // 放入视频转换队列
        uploadFileMapper.updateStatus(uploader.getId(), UploadFile.STATUS_COMPLETED);
        videoService.put2transcode(get(uploader.getId()));
    }

    @Override
    public void completedError(FileUploader uploader) {
        uploadFileMapper.updateStatus(uploader.getId(), UploadFile.STATUS_UPLOAD_ERROR);
    }

    @Override
    public void uploadChunkSuccess(FileUploader uploader, int chunkIndex) {
        if (uploadFileMapper.updateFinishChunk(uploader.getId()) == 0) {
            throw new BusinessException("无法更新上传文件状态");
        }
    }

    @Override
    public void uploadChunkError(FileUploader uploader, int chunkIndex, Throwable throwable) {

    }

    @Override
    public void uploadError(FileUploader uploader) {
        uploadFileMapper.updateStatus(uploader.getId(), UploadFile.STATUS_UPLOAD_ERROR);
    }


}
