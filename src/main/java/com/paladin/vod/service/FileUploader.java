package com.paladin.vod.service;

import com.paladin.framework.exception.BusinessException;
import com.paladin.vod.model.UploadFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUploader {

    private String id;
    private int chunkCount;
    private long chunkSize;
    private long fileSize;

    private String storagePath;
    private String filePath;
    private String realFilePath;
    private int currentChunk;

    private long lastUpdateTime;

    private boolean completed;

    private int maxErrorTimes = 5;
    private int errorTimes = 0;

    private FileUploadListener uploadListener;

    public FileUploader(UploadFile uploadFile, FileUploadListener uploadListener, String targetFolder) {
        this.id = uploadFile.getId();
        this.uploadListener = uploadListener;
        this.chunkSize = 1024L * 1024 * uploadFile.getChunkSize();
        this.fileSize = uploadFile.getFileSize();

        this.chunkCount = (int) (fileSize / chunkSize);
        if (fileSize % chunkSize != 0) {
            this.chunkCount++;
        }

        this.currentChunk = uploadFile.getFinishChunk();

        if (!targetFolder.endsWith("/")) {
            targetFolder += "/";
        }

        this.storagePath = targetFolder + uploadFile.getServerRelativePath();

        // 创建目录
        Path path = null;
        try {
            path = Paths.get(storagePath);
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            } else if (!Files.isDirectory(path)) {
                Files.deleteIfExists(path);
                Files.createDirectory(path);
            }
        } catch (FileAlreadyExistsException e1) {
            if (!Files.isDirectory(path)) {
                try {
                    Files.deleteIfExists(path);
                    Files.createDirectory(path);
                } catch (IOException e) {
                    throw new BusinessException("创建临时文件失败", e);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("创建临时文件失败", e);
        }

        // 最终完成文件地址
        this.realFilePath = storagePath + id + uploadFile.getSuffix();
        // 读取文件
        this.filePath = storagePath + id + ".tmp";
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized void uploadFileChunk(int index, InputStream inputStream) {
        if (completed || index != currentChunk) {
            return;
        }

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw")) {
            long offset = currentChunk * chunkSize;
            randomAccessFile.seek(offset);

            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                randomAccessFile.write(buffer, 0, n);
            }

            // 成功写入一块后，重置错误次数
            errorTimes = 0;

            try {
                uploadListener.uploadChunkSuccess(this, currentChunk++);
            } catch (Exception e) {
                log.error("上传监听异常，将关闭上传", e);
                close();
                return;
            }

        } catch (IOException e) {
            log.error("上传文件块异常", e);

            try {
                uploadListener.uploadChunkError(this, currentChunk, e);
            } catch (Exception e1) {
                log.error("上传监听异常，将关闭上传", e);
                close();
                return;
            }

            // 达到一定错误次数，则直接设为异常不再进行
            if (++errorTimes >= maxErrorTimes) {
                close();
                try {
                    uploadListener.uploadError(this);
                } catch (Exception e1) {
                    log.error("上传监听异常，将关闭上传", e);
                    return;
                }
            }
        }

        lastUpdateTime = System.currentTimeMillis();

        if (currentChunk == chunkCount) {
            close();
            try {
                File tmpFile = new File(filePath);
                File newFile = new File(realFilePath);
                // 重命名
                if (tmpFile.renameTo(newFile)) {
                    uploadListener.completedSuccess(this);
                } else {
                    log.error("重命名文件[" + filePath + "]到[" + realFilePath + "]异常");
                    uploadListener.completedError(this);
                }
            } catch (Exception e) {
                log.error("重命名文件异常", e);
            }
        }
    }

    /**
     * 置为完成状态，不在进行上传
     */
    public void close() {
        completed = true;
    }


    public String getId() {
        return id;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public int getCurrentChunk() {
        return currentChunk;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }


    /**
     * 上传监听抛出的异常都将导致上传停止
     */
    public interface FileUploadListener {
        // 文件上传完毕，并且成功重命名
        void completedSuccess(FileUploader uploader);

        // 文件上传完毕，但是最终出现异常（文件重命名出现异常）
        void completedError(FileUploader uploader);

        // 上传块成功，应该更新上传进度
        void uploadChunkSuccess(FileUploader uploader, int chunkIndex);

        // 上传块异常
        void uploadChunkError(FileUploader uploader, int chunkIndex, Throwable throwable);

        // 上传异常，应该结束该次上传
        void uploadError(FileUploader uploader);
    }

}
