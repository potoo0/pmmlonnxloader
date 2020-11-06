package com.beginner.pmmlonnxloader.utils;

import ai.onnxruntime.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OnnxModelLoader {
  private final static Logger logger = LogManager.getLogger(OnnxModelLoader.class);

  private static Path getResourcePath(String path) {
    return new File(OnnxModelLoader.class.getResource(path).getFile()).toPath();
  }

  public static Map<String, Object> loadAndPredict(String modelPath, Map inputMap) throws OrtException {
    // 加载方式 1: 加载 resource 文件夹下的模型，
    //    要求模型文件在 resource 文件夹下，即被打包进 jar，而新上传的模型文件不在 jar 包内
    // modelPath = getResourcePath(modelPath).toString();

    // 加载方式 2:(推荐) 加载 jar 运行目录下的模型
    if (!Character.isLetterOrDigit(modelPath.charAt(0))) {
      modelPath = modelPath.substring(1);
    }
    modelPath = System.getProperty("user.dir") + File.separator + modelPath;

    logger.info("modelPath: " + modelPath);

    try (
        OrtEnvironment env = OrtEnvironment.getEnvironment(
            OrtLoggingLevel.ORT_LOGGING_LEVEL_FATAL, "a_multiply_b_add_c");
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        OrtSession session = env.createSession(modelPath, options)
    ) {
      // 检查模型是否成功加载
      if (session == null) {
        logger.error("模型加载失败");
        return null;
      }

      // 模型的元数据
      Map<String, String> modelMetaMap = new HashMap<>();
      OnnxModelMetadata metadata = session.getMetadata();
      modelMetaMap.put("ProducerName", metadata.getProducerName());
      modelMetaMap.put("GraphName", metadata.getGraphName());
      modelMetaMap.put("Domain", metadata.getDomain());
      modelMetaMap.put("Description", metadata.getDescription());
      modelMetaMap.put("Version", String.valueOf(metadata.getVersion()));
      modelMetaMap.put("CustomMetadataIsEmpty", String.valueOf(metadata.getCustomMetadata().isEmpty()));
      logger.info("Model meta data: " + modelMetaMap);

      // 模型推理前必须检查模型输入
      if (session.getNumInputs() != inputMap.size()) {
        logger.error("输入参数与模型输入不匹配");
        return null;
      }

      // 构建模型输入，输入必须是与模型输入类型对应的基本数据类型！
      // 即只能是 int, float, double 之一，并且不能是 Integer 等包装类
      Map<String, OnnxTensor> inputTensorMap = new HashMap<>();
      for (String inputName : session.getInputNames()) {
        inputTensorMap.put(inputName, OnnxTensor.createTensor(env, inputMap.get(inputName)));
      }
      logger.info("inputTensorMap: " + inputTensorMap);

      // 构建模型输出: 此处获取模型所有输出
      Set<String> requestedOutputs = new HashSet<>(session.getOutputNames());

      Map<String, Object> outputMap = new HashMap<>();
      try (
          OrtSession.Result results = session.run(inputTensorMap, requestedOutputs)
      ){
        // 包装模型输出到 Map
        for (Map.Entry<String, OnnxValue> r : results) {
          OnnxValue resultValue = r.getValue();
          OnnxTensor resultTensor = (OnnxTensor) resultValue;
          outputMap.put(r.getKey(), resultTensor.getValue());
        }
      } catch (OrtException e) {
        logger.error("模型推理失败: " + modelPath);
        e.printStackTrace();
      }
      return outputMap;
    }
  }
}
