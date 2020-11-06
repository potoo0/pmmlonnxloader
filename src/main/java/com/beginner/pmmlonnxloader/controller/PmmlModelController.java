package com.beginner.pmmlonnxloader.controller;

import com.alibaba.fastjson.JSONObject;
import com.beginner.pmmlonnxloader.utils.ModelDownloadUtil;
import com.beginner.pmmlonnxloader.utils.PmmlModelLoader;
import org.jpmml.evaluator.Evaluator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("pmml")
public class PmmlModelController {
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

    InputStream pmmlStream = PmmlModelLoader.getPMMLStream(modelPath);
    Evaluator pmmlEvaluator = PmmlModelLoader.getPMMLEvaluator(pmmlStream);

    // 预测结果封装到 json 中
    JSONObject result = new JSONObject();
    result.put("code", "200");
    result.put("msg", "ok");
    result.put("modelName", modelPath);
    result.put("data", JSONObject.toJSON(PmmlModelLoader.predict(pmmlEvaluator, modelInputMap)));

    return result.toJSONString();

  }
}
