/*
 * Copyright (C) 2016-2020 IMassBank Corporation
 *
 */
package com.opc.common.utils.net;

import com.google.common.collect.Lists;
import com.opc.common.exception.BizException;
import lombok.Data;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * 通用类: FTP 处理通用
 *
 * @author 莫问
 */
@Data
public class FtpUtil {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    /**
     * Ftp服务器
     */
    private String server;

    /**
     * Ftp端口
     */
    private int port;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 从 FTP 指定得路径
     */
    private String remotePath;

    /**
     * 下载后要放置得路径
     */
    private String localPath;

    /**
     * 是否初始化参数
     */
    private boolean init;

    /**
     * 构造器
     *
     * @param server   服务IP或URL
     * @param port     端口号
     * @param userName 账号
     * @param password 密码
     */
    public FtpUtil(String server, int port, String userName, String password) {
        this.server = server;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.init = Boolean.TRUE;
    }

    /**
     * 连接FTP服务器
     *
     * @return 连接FTP客户端
     */
    private FTPClient connectFTPServer() {

        if (!init) {
            throw new BizException("请先实例化对象参数---【FtpUtil】");
        }

        FTPClient ftp = new FTPClient();
        try {
            //配置FTP连接参数
            ftp.configure(getFTPClientConfig());
            //连接FTP服务器
            ftp.connect(server, port);

            if ((!FTPReply.isPositiveCompletion(ftp.getReplyCode()))) {
                // 关闭Ftp连接
                closeFTPClient(ftp);
                throw new BizException("连接FTP服务器失败,请检查! ReplyCode = : {}", ftp.getReplyCode());
            }

            if (!ftp.login(userName, password)) {
                throw new BizException("login failed(登陆失败)");
            }

            // 文件类型,默认是ASCII
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            // 设置被动模式
            ftp.enterLocalPassiveMode();
            ftp.setConnectTimeout(2000);
            ftp.setControlEncoding("UTF-8");

            //返回对象
            return ftp;
        } catch (Exception e) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (Exception e1) {
                    throw new BizException("连接失败");
                }
            }
            throw new BizException("连接失败");
        }
    }

    /**
     * 配置FTP连接参数
     *
     * @return FTP配置实例
     */
    private FTPClientConfig getFTPClientConfig() {

        String systemKey = FTPClientConfig.SYST_UNIX;
        String serverLanguageCode = "zh";
        FTPClientConfig conf = new FTPClientConfig(systemKey);
        conf.setServerLanguageCode(serverLanguageCode);
        conf.setDefaultDateFormatStr("yyyy-MM-dd");

        return conf;
    }

    /**
     * 从FTP指定的路径下载文件
     *
     * @return 下载成功（true） 下载失败（false）
     */
    public boolean downloadFile() {

        FTPClient ftpClient = connectFTPServer();

        OutputStream output = null;
        try {
            if (checkFileExistCreate(localPath)) {
                File file = new File(localPath);
                output = new FileOutputStream(file);
                boolean result = ftpClient.retrieveFile(remotePath, output);
                if (!result) {
                    throw new BizException("从指定FTP路径下载失败  【server】：{} 【remotePath】：{}", server, remotePath);
                }
                return true;
            } else {
                throw new BizException("本地路径不存在或创建本地路径失败 【localPath】：{}", localPath);
            }
        } catch (Exception e) {
            throw new BizException("从FTP指定路径下载出现异常 【server】：{} 【remotePath】：{}", server, remotePath, e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    // 做监控

                    throw new BizException("从FTP指定路径下载出现异常 【server】：{} 【remotePath】：{}", server, remotePath, e);
                }
            }
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    // 做监控

                    throw new BizException("从FTP指定路径下载完释放连接错误 【server】：{} 【remotePath】：{}", server, remotePath, e);
                }
            }
        }
    }

    /**
     * 从FTP指定的路径下载文件
     *
     * @param remoteFilePath 指定服务器的路径
     * @return 返回文件流
     */
    public InputStream downFile(String remoteFilePath) throws IOException {
        FTPClient ftpClient = connectFTPServer();
        return ftpClient.retrieveFileStream(remoteFilePath);
    }

    /**
     * 获取FTP服务器上指定路径下的文件列表（25）
     *
     * @param filePath 指定服务器路径
     * @return 返回文件列表（25）
     */
    public List<String> getFtpServerFileList(String filePath) throws IOException {
        FTPClient ftpClient = connectFTPServer();
        FTPListParseEngine engine = ftpClient.initiateListParsing(filePath);
        List<FTPFile> ftpFiles = Arrays.asList(engine.getNext(25));

        return getFTPServerFileList(ftpFiles);
    }

    /**
     * 获取FTP服务器上指定路径下的文件列表
     *
     * @param path 指定服务器路径
     * @return 返回文件列表
     */
    public List<String> getFileList(String path) throws IOException {

        FTPClient ftpClient = connectFTPServer();
        List<FTPFile> ftpFiles = Arrays.asList(ftpClient.listFiles(path));

        return getFTPServerFileList(ftpFiles);
    }

    /**
     * 列出FTP服务器文件列表信息
     *
     * @param ftpFiles 文件信息
     * @return 返回文件名
     */
    public List<String> getFTPServerFileList(List<FTPFile> ftpFiles) {
        List<String> files = Lists.newArrayList();
        if (ftpFiles == null || ftpFiles.size() == 0) {
            return files;
        }
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isFile()) {
                files.add(ftpFile.getName());
            }
        }
        return files;
    }

    /**
     * 改变工作目录，如失败则创建文件夹
     *
     * @param remoteFolderPath 服务器的文件地址
     */
    public void changeDirectory(String remoteFolderPath) throws IOException {

        FTPClient ftpClient = connectFTPServer();

        if (remoteFolderPath != null) {
            boolean flag = ftpClient.changeWorkingDirectory(remoteFolderPath);
            if (!flag) {
                ftpClient.makeDirectory(remoteFolderPath);
                ftpClient.changeWorkingDirectory(remoteFolderPath);
            }
        }

    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 存在（true） 不存在（false）
     */
    public boolean checkFileExist(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 检查文件是否存在 不存在就创建
     *
     * @param filePath 文件路径
     * @return 存在（true） 不存在（false）
     */
    public boolean checkFileExistCreate(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("文件不存在,创建文件" + filePath);
            return file.createNewFile();
        }
        return Boolean.TRUE;
    }

    /**
     * 获取文件名,不包括后缀
     *
     * @param filePath 文件路径
     * @return 存在（文件名）不存在（null）
     */
    public String getFileNamePrefix(String filePath) {

        boolean flag = this.checkFileExist(filePath);
        if (flag) {
            File file = new File(filePath);
            String fileName = file.getName();
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return null;
    }

    /**
     * 关闭FTP连接
     *
     * @param ftp 客户端
     */
    public void closeFTPClient(FTPClient ftp) {

        try {
            if (ftp != null && ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception e) {
            throw new BizException("关闭FTP连接异常 【{}】", e.getMessage(), e);
        }
    }
}


