package com.beginner.pmmlonnxloader;

import ai.onnxruntime.OrtException;
import com.alibaba.fastjson.JSONObject;
import com.beginner.pmmlonnxloader.utils.ModelDownloadUtil;
import com.beginner.pmmlonnxloader.utils.OnnxModelLoader;
import com.beginner.pmmlonnxloader.utils.PmmlModelLoader;
import org.jpmml.evaluator.Evaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class PmmlonnxloaderApplicationTests {

  @Test
  void testPMML() {
    HashMap<String, Object> modelInputMap = new HashMap<>();
    modelInputMap.put("x1", 1.2);
    modelInputMap.put("x2", 2.2);
    modelInputMap.put("x3", 3.4);

    String pmmlModelPath = "models/a_multiply_b_add_c.pmml";
    InputStream pmmlStream = PmmlModelLoader.getPMMLStream(pmmlModelPath);
    Evaluator pmmlEvaluator = PmmlModelLoader.getPMMLEvaluator(pmmlStream);

    // 预测结果封装到 json 中
    JSONObject result = new JSONObject();
    result.put("code", "200");
    result.put("msg", "ok");
    result.put("modelName", pmmlModelPath);
    result.put("data", JSONObject.toJSON(PmmlModelLoader.predict(pmmlEvaluator, modelInputMap)));

    System.out.println(result.toJSONString());

  }

  @Test
  void testOnnx() {
    String onnxModelPath = "/models/a_multiply_b_add_c.onnx";

    Map<String, Object> modelInputMap = new HashMap<>();
    modelInputMap.put("float_input", new float[][]{{1f, 2f, 3f}});

    Map<String, Object> modelOutputMap = new HashMap<>();
    try {
      modelOutputMap = OnnxModelLoader.loadAndPredict(onnxModelPath, modelInputMap);
    } catch (OrtException e) {
      e.printStackTrace();
    }

    // 预测结果封装到 json 中
    JSONObject result = new JSONObject();
    result.put("code", "200");
    result.put("msg", "ok");
    result.put("modelName", onnxModelPath);
    result.put("data", JSONObject.toJSON(modelOutputMap));

    System.out.println(result.toJSONString());
  }

  @Test
  void testURLDownload() {
    String ftpUrl = "ftp://ftp1:12@localhost/mlflowModel/9/1/a_multiply_b_add_c.onnx";

    String onnxModelPath = ModelDownloadUtil.downloader(ftpUrl);

    Map<String, Object> modelInputMap = new HashMap<>();
    modelInputMap.put("float_input", new float[][]{{1f, 2f, 3f}});

    Map<String, Object> modelOutputMap = new HashMap<>();
    try {
      modelOutputMap = OnnxModelLoader.loadAndPredict(onnxModelPath, modelInputMap);
    } catch (OrtException e) {
      e.printStackTrace();
    }

    // 预测结果封装到 json 中
    JSONObject result = new JSONObject();
    result.put("code", "200");
    result.put("msg", "ok");
    result.put("modelName", onnxModelPath);
    result.put("data", JSONObject.toJSON(modelOutputMap));

    System.out.println(result.toJSONString());
  }

}
