package com.paladin.framework.service.impl;

import com.paladin.framework.service.FileStoreService;
import com.paladin.framework.utils.file.FtpHelper;
import org.apache.commons.net.ftp.FTPClient;

import java.io.InputStream;

/**
 * FTP方式存储文件服务实现
 *
 * @author TontoZhou
 * @since 2020/6/28
 */
public class FtpFileStoreService implements FileStoreService {

    private String host;
    private int port;
    private String username;
    private String password;

    private String baseVisitUrl;

    public FtpFileStoreService(String host, int port, String username, String password, String visitHost, int visitPort) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        this.baseVisitUrl = "http://" + visitHost + ":" + visitPort + "/";
    }

    @Override
    public void storeFile(InputStream input, String filePath, String fileName) {
        FTPClient ftp = FtpHelper.getFTPClient(host, port, username, password);
        FtpHelper.uploadFile(ftp, input, filePath, fileName);
        FtpHelper.closeFTP(ftp);
    }

    @Override
    public void deleteFile(String filePath, String fileName) {
        FTPClient ftp = FtpHelper.getFTPClient(host, port, username, password);
        FtpHelper.deleteFile(ftp, filePath, fileName);
        FtpHelper.closeFTP(ftp);
    }

    @Override
    public void checkAndMakeDirectory(String filePath) {
        // do nothing 在存储文件时会检查是否存在文件
    }

    @Override
    public String getStoreType() {
        return "ftp";
    }

    @Override
    public String getFileUrl(String relativePath) {
        return baseVisitUrl + relativePath;
    }
}
