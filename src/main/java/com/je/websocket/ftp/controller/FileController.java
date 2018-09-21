package com.je.websocket.ftp.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.je.websocket.ftp.componet.FTPUtil;

@Controller
public class FileController {

	@Autowired
	private FTPUtil ftpUtil;

	@RequestMapping("/add")
	@ResponseBody
	public Object add(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
		try {
			List<Map<String, String>> list = new ArrayList<>();
			String path = null;
			Map<String, String> map = null;
			for (MultipartFile f : files) {
				map = new HashMap<>();
				map.put("fileName", f.getOriginalFilename());
				path = ftpUtil.uploadFile(f.getInputStream(), f.getOriginalFilename());
				if (path != null) {
					map.put("savePath", path);
				} else {
					map.put("savePath", null);
				}
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			return "0";
		}
	}

	@RequestMapping("/down")
	public void down(HttpServletResponse response, HttpServletRequest request, String fileName) throws Exception {
		Thread.sleep(500);
		File file = new File("/Users/shengte/vbox/" + fileName);
		File info = new File("/Users/shengte/vbox/" + fileName + ".info");
		if (info.exists()) {
			FileInputStream in = new FileInputStream(info);
			// size 为字串的长度 ，这里一次性读完
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			String str = new String(buffer, "GB2312");
			Long s1 = new Long(str);
			while (s1 > file.length() - 100) {
				System.out.println("wait");
				Thread.sleep(500);
				file = new File("/Users/shengte/vbox/" + fileName);
			}
		} else {
			file = ftpUtil.getInputStream(fileName);
		}
		if (file == null)
			return;
		responseFile(new BufferedInputStream(new FileInputStream("/Users/shengte/vbox/" + fileName)), file.length(),
				fileName, request, response);
	}

	public void responseFile(InputStream in, long FileSize, String fileName, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.reset();
		response.setCharacterEncoding("utf-8");
		response.setContentType(setContentType(fileName));
		response.setHeader("Accept-Ranges", "bytes");
		// String agent = request.getHeader("User-Agent");
		// if (StringUtils.isNotBlank(agent) && agent.indexOf("FIREFOX") != -1) {
		// response.setHeader("Content-Disposition",
		// "attachment;filename=" + new String(fileName.getBytes("GB2312"),
		// "ISO-8859-1"));
		// } else {
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
		// }
		long fSize = 0;
		fSize = FileSize;
		long start = 0;
		long end = 0;
		long contentLength = 0;
		String range = request.getHeader("Range");
		/**
		 * range的形式:a-,-a,a-b等
		 */
		if (null != range) {
			// ddxc
			range = range.replaceAll("bytes=", "");
			try {
				// -a
				if (range.startsWith("-")) {
					contentLength = Long.parseLong(range.replaceAll("-", ""));
					end = fSize - 1;
					start = fSize - contentLength;
				}
				// a-
				else if (range.endsWith("-")) {
					start = Long.parseLong(range.replaceAll("-", ""));
					end = fSize - 1;
					contentLength = fSize - start;
				}
				// a-b
				else {
					String[] se = range.split("-");
					start = Long.parseLong(se[0]);
					end = Long.parseLong(se[1]);
					contentLength = end - start + 1;
				}
			} catch (Exception e) {
				// logger.error(e.getLocalizedMessage());
				start = 0;
			}
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			String contentRange = new StringBuffer("bytes ").append(start + "").append("-").append((end) + "")
					.append("/").append(fSize + "").toString();
			response.setHeader("Content-Range", contentRange);
			response.addHeader("Content-Length", String.valueOf(contentLength));
		} else {
			response.addHeader("Content-Length", String.valueOf(fSize));
		}
		ServletOutputStream out = response.getOutputStream();
		in.skip(start);
		try {
			byte[] buffer = new byte[1024 * 100];
			int length = 0;
			while ((length = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, length);
				Thread.sleep(100);
			}
			out.flush();
		} catch (Exception e) {
			// logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}

	}

	public static String setContentType(String returnFileName) {
		String contentType = "application/octet-stream";
		if (returnFileName.lastIndexOf(".") < 0)
			return contentType;
		returnFileName = returnFileName.toLowerCase();
		returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".") + 1);
		if (returnFileName.equals("html") || returnFileName.equals("htm") || returnFileName.equals("shtml")) {
			contentType = "text/html";
		} else if (returnFileName.equals("css")) {
			contentType = "text/css";
		} else if (returnFileName.equals("xml")) {
			contentType = "text/xml";
		} else if (returnFileName.equals("gif")) {
			contentType = "image/gif";
		} else if (returnFileName.equals("jpeg") || returnFileName.equals("jpg")) {
			contentType = "image/jpeg";
		} else if (returnFileName.equals("js")) {
			contentType = "application/x-javascript";
		} else if (returnFileName.equals("atom")) {
			contentType = "application/atom+xml";
		} else if (returnFileName.equals("rss")) {
			contentType = "application/rss+xml";
		} else if (returnFileName.equals("mml")) {
			contentType = "text/mathml";
		} else if (returnFileName.equals("txt")) {
			contentType = "text/plain";
		} else if (returnFileName.equals("jad")) {
			contentType = "text/vnd.sun.j2me.app-descriptor";
		} else if (returnFileName.equals("wml")) {
			contentType = "text/vnd.wap.wml";
		} else if (returnFileName.equals("htc")) {
			contentType = "text/x-component";
		} else if (returnFileName.equals("png")) {
			contentType = "image/png";
		} else if (returnFileName.equals("tif") || returnFileName.equals("tiff")) {
			contentType = "image/tiff";
		} else if (returnFileName.equals("wbmp")) {
			contentType = "image/vnd.wap.wbmp";
		} else if (returnFileName.equals("ico")) {
			contentType = "image/x-icon";
		} else if (returnFileName.equals("jng")) {
			contentType = "image/x-jng";
		} else if (returnFileName.equals("bmp")) {
			contentType = "image/x-ms-bmp";
		} else if (returnFileName.equals("svg")) {
			contentType = "image/svg+xml";
		} else if (returnFileName.equals("jar") || returnFileName.equals("var") || returnFileName.equals("ear")) {
			contentType = "application/java-archive";
		} else if (returnFileName.equals("doc")) {
			contentType = "application/msword";
		} else if (returnFileName.equals("pdf")) {
			contentType = "application/pdf";
		} else if (returnFileName.equals("rtf")) {
			contentType = "application/rtf";
		} else if (returnFileName.equals("xls")) {
			contentType = "application/vnd.ms-excel";
		} else if (returnFileName.equals("ppt")) {
			contentType = "application/vnd.ms-powerpoint";
		} else if (returnFileName.equals("7z")) {
			contentType = "application/x-7z-compressed";
		} else if (returnFileName.equals("rar")) {
			contentType = "application/x-rar-compressed";
		} else if (returnFileName.equals("swf")) {
			contentType = "application/x-shockwave-flash";
		} else if (returnFileName.equals("rpm")) {
			contentType = "application/x-redhat-package-manager";
		} else if (returnFileName.equals("der") || returnFileName.equals("pem") || returnFileName.equals("crt")) {
			contentType = "application/x-x509-ca-cert";
		} else if (returnFileName.equals("xhtml")) {
			contentType = "application/xhtml+xml";
		} else if (returnFileName.equals("zip")) {
			contentType = "application/zip";
		} else if (returnFileName.equals("mid") || returnFileName.equals("midi") || returnFileName.equals("kar")) {
			contentType = "audio/midi";
		} else if (returnFileName.equals("mp3")) {
			contentType = "audio/mpeg";
		} else if (returnFileName.equals("ogg")) {
			contentType = "audio/ogg";
		} else if (returnFileName.equals("m4a")) {
			contentType = "audio/x-m4a";
		} else if (returnFileName.equals("ra")) {
			contentType = "audio/x-realaudio";
		} else if (returnFileName.equals("3gpp") || returnFileName.equals("3gp")) {
			contentType = "video/3gpp";
		} else if (returnFileName.equals("mp4")) {
			contentType = "video/mp4";
		} else if (returnFileName.equals("mpeg") || returnFileName.equals("mpg")) {
			contentType = "video/mpeg";
		} else if (returnFileName.equals("mov")) {
			contentType = "video/quicktime";
		} else if (returnFileName.equals("flv")) {
			contentType = "video/x-flv";
		} else if (returnFileName.equals("m4v")) {
			contentType = "video/x-m4v";
		} else if (returnFileName.equals("mng")) {
			contentType = "video/x-mng";
		} else if (returnFileName.equals("asx") || returnFileName.equals("asf")) {
			contentType = "video/x-ms-asf";
		} else if (returnFileName.equals("wmv")) {
			contentType = "video/x-ms-wmv";
		} else if (returnFileName.equals("avi")) {
			contentType = "video/x-msvideo";
		}
		return contentType;
	}

}
