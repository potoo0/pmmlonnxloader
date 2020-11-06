package com.beginner.pmmlonnxloader.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ModelDownloadUtil {
  private static final int BUFFER_SIZE = 4096;
  private final static String MODEL_DIR = "models";

  private final static Logger logger = LogManager.getLogger(ModelDownloadUtil.class);

  private static void createModelDir() {
    File folder = new File(MODEL_DIR);
    if (!folder.exists() && !folder.isDirectory()) {
      if (folder.mkdirs()) {
        logger.info("模型父目录 {} 创建成功", MODEL_DIR);
      } else {
        logger.error("模型父目录 {} 创建失败!!!", MODEL_DIR);
        throw new RuntimeException("Could not create folder for models!");
      }
    }
  }

  public static String downloader(String ftpUrl) {
    createModelDir();

    logger.info("model url: " + ftpUrl);

    String saveDir = System.getProperty("user.dir") + File.separator + MODEL_DIR;

    String random = String.valueOf(System.currentTimeMillis() % 1000);
    String fileName = random + ftpUrl.substring(ftpUrl.lastIndexOf("/") + 1);

    String filePath = saveDir + File.separator + fileName;

    logger.info("filePath: " + filePath);

    try {
      URL url = new URL(ftpUrl);
      URLConnection conn = url.openConnection();
      InputStream inputStream = conn.getInputStream();

      FileOutputStream outputStream = new FileOutputStream(filePath);

      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = -1;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      outputStream.close();
      inputStream.close();

      logger.info("model downloaded");
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return MODEL_DIR + File.separator + fileName;
  }
}
