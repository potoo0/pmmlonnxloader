package com.beginner.pmmlonnxloader.controller;

import ai.onnxruntime.OrtException;
import com.alibaba.fastjson.JSONObject;
import com.beginner.pmmlonnxloader.utils.ModelDownloadUtil;
import com.beginner.pmmlonnxloader.utils.OnnxModelLoader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("onnx")
public class OnnxModelController {

  /**
   * 包装输入数据类型为 Map 内嵌 float 基本数据类型
   * <p>
   * TODO：目前此函数只支持二维
   *
   * @param dataList: List 的原数据
   * @return: float[][]
   **/
  static float[][] wrapInput(List dataList) {
    float[][] array2Dim = new float[dataList.size()][];

    for (int i1 = 0; i1 < dataList.size(); i1++) {
      Object lis = dataList.get(i1);
      if (lis instanceof List) {
        List trueLis = ((List) lis);

        if (!(trueLis.get(0) instanceof List)) {
          float[] ids = new float[trueLis.size()];
          for (int i = 0; i < trueLis.size(); i++) {
            ids[i] = Float.parseFloat(trueLis.get(i).toString());
          }
          array2Dim[i1] = ids;
        } else {
          System.out.println("输入维度大于二维!");
        }
      } else {
        System.out.println("输入维度小于二维!");
      }
    }
    return array2Dim;
  }

  @RequestMapping(value = "/json", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
  public String getByJSON(@RequestBody JSONObject jsonParam) {
    String modelPath = (String) jsonParam.get("modelPath");
    if (modelPath == null) {
      JSONObject result = new JSONObject();
      result.put("code", "500");
      result.put("msg", "illegal or empty modelPath");
      return result.toJSONString();
    }

    if (modelPath.startsWith("ftp")){
      modelPath = ModelDownloadUtil.downloader(modelPath);
    }

    Map modelInputMap = (Map) ((List) jsonParam.get("X")).get(0);
    for (Object key : modelInputMap.keySet()) {
      if (modelInputMap.get(key) instanceof java.util.List) {
        modelInputMap.put(key, wrapInput((List) modelInputMap.get(key)));
      }
    }

    Map<String, Object> modelOutputMap = new HashMap<>();
    try {
      modelOutputMap = OnnxModelLoader.loadAndPredict(modelPath, modelInputMap);
    } catch (OrtException e) {
      e.printStackTrace();
    }

    // 预测结果封装到 json 中
    JSONObject result = new JSONObject();

    result.put("code", "200");
    result.put("msg", "ok");
    result.put("modelPath", modelPath);
    result.put("data", JSONObject.toJSON(modelOutputMap));

    return result.toJSONString();
  }
}
