package com.je.websocket.ftp.componet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class FTPUtil {
	private Logger logger = LoggerFactory.getLogger(FTPUtil.class);
	private String ftpIp = "10.18.0.5";
	private String ftpUser = "dapp";
	private String ftpPass = "3c66AwKES.";
	private FTPClient ftpClient;
	private String basePath = "video";

	/**
	 * 对外暴露的上传文件方法
	 * 
	 * @param fileList
	 * @return
	 */
	public String uploadFile(InputStream in, String fileName) throws IOException {
		logger.info("开始连接FTP服务器");
		// 把异常抛给service层，不在此处理

		boolean result = uploadFile(basePath, in, fileName);
		logger.info("开始连接FTP服务器，结束上传，上传结果{}", result);
		if (result) {
			return basePath + "/" + fileName;
		}
		return null;
	}

	private boolean uploadFile(String remotePath, InputStream in, String fileName) throws IOException {
		boolean uploaded = true;
		// 连接FTP服务器
		if (connectServer(ftpIp, ftpUser, ftpPass)) {
			try {
				ftpClient.changeWorkingDirectory(remotePath);
				ftpClient.setBufferSize(1024);
				ftpClient.setControlEncoding("UTF-8");
				// 设置成二进制格式可以防止乱码
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				// 被动模式存储
				ftpClient.enterLocalPassiveMode();
				// 调用storeFile方法存储
				ftpClient.storeFile(fileName, in);
			} catch (IOException e) {
				logger.error("上传文件异常", e);
				uploaded = false;
			} finally {
				// 关闭连接和文件流
				in.close();
				ftpClient.disconnect();
			}
		}
		return uploaded;

	}

	/**
	 * 连接FTP服务器
	 * 
	 * @param ip
	 * @param user
	 * @param pwd
	 * @return
	 */
	private boolean connectServer(String ip, String user, String pwd) {
		boolean isSuccess = false;
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(ip);
			isSuccess = ftpClient.login(user, pwd);
		} catch (IOException e) {
			logger.error("FTP服务器连接失败", e);

		}
		return isSuccess;
	}

	public File getInputStream(String fileFullName) throws IOException {
		StopWatch stopWatch = new StopWatch("FTP 读取");
		stopWatch.start("FTP 连接");
		try {
			if (connectServer(ftpIp, ftpUser, ftpPass)) {
				System.out.println("link.......");
				stopWatch.stop();
				ftpClient.changeWorkingDirectory(basePath);
				ftpClient.setBufferSize(1024);
				ftpClient.setControlEncoding("UTF-8");
				// 设置成二进制格式可以防止乱码
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				// 被动模式存储
				ftpClient.enterLocalPassiveMode();
				FTPFile[] fs = ftpClient.listFiles();
				File localFile = null;
				for (FTPFile f : fs) {
					String _fname = f.getName();
					if (_fname.toUpperCase().equals(fileFullName.toUpperCase())) {
						stopWatch.start("FTP 流读取");
						localFile = new File("/Users/shengte/vbox/" + f.getName());
						File info = new File("/Users/shengte/vbox/" + f.getName() + ".info");
						FileWriter fw = null;
						if (!info.exists()) {
							info.createNewFile();
						}
						fw = new FileWriter(info);
						BufferedWriter out = new BufferedWriter(fw);
						String s = f.getSize() + "";
						out.write(s, 0, s.length() - 1);
						out.close();
						// 输出流
						OutputStream os = new FileOutputStream(localFile);
						ftpClient.retrieveFile(f.getName(), os);
						os.close();
						ftpClient.logout();
						stopWatch.stop();
						break;
					}
				}
				System.out.println(stopWatch.prettyPrint());
				return localFile;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ftpClient.disconnect();
		}
		System.out.println(stopWatch.prettyPrint());
		return null;
	}

}
