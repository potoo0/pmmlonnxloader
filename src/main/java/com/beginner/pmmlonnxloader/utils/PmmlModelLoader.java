package com.beginner.pmmlonnxloader.utils;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PmmlModelLoader {

  public static InputStream getPMMLStream(String modelPath) {
    File file = Paths.get(System.getProperty("user.dir"), modelPath).toFile();
    InputStream is = null;
    try {
      is = new FileInputStream(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return is;
  }

  public static Evaluator getPMMLEvaluator(InputStream is) {
    ModelEvaluatorBuilder modelEvaluatorBuilder = null;
    ModelEvaluator<?> modelEvaluator = null;

    try {
      // LoadingModelEvaluatorBuilder 线程不安全，
      // 使用 loadingModelEvaluatorBuilder.clone().load() 使每个线程维护一个副本
      modelEvaluatorBuilder = new LoadingModelEvaluatorBuilder().load(is);
    } catch ( SAXException | JAXBException e) {
      e.printStackTrace();
    }

    if (modelEvaluatorBuilder != null) {
      modelEvaluator = modelEvaluatorBuilder.build();
      modelEvaluator.verify();
    }

    return modelEvaluator;
  }

  public static Map<String, Object> predict(Evaluator evaluator, Map<String, Object> paramData) {
    if (evaluator == null || paramData == null) {
      System.out.println("evaluator 或 dataMap 为空,");
      return null;
    }

    List<InputField> inputFields = evaluator.getInputFields();   //获取模型的输入域
    Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

    for (InputField inputField : inputFields) {  //将参数通过模型对应的名称进行添加
      FieldName inputFieldName = inputField.getName();   //获取模型中的参数名
      Object paramValue = paramData.get(inputFieldName.getValue());  //获取参数值
      FieldValue fieldValue = inputField.prepare(paramValue);   //将参数值传入模型中的参数中
      // System.out.println("\n\n inputFieldName: " + inputFieldName);
      // System.out.println("paramValue: " + paramValue);
      arguments.put(inputFieldName, fieldValue);
    }

    // 预测
    Map<FieldName, ?> results = evaluator.evaluate(arguments);
    List<TargetField> targetFields = evaluator.getTargetFields();

    // 将预测结果封装到 map
    Map<String, Object> resultMap = new HashMap<>();

    for(TargetField targetField : targetFields) {
      FieldName targetFieldName = targetField.getName();
      Object targetFieldValue = results.get(targetFieldName);
      if (targetFieldValue instanceof Computable) {
        Computable computable = (Computable) targetFieldValue;
        resultMap.put(targetFieldName.getValue(), computable.getResult());
      }else {
        resultMap.put(targetFieldName.getValue(), targetFieldValue);
      }
    }

    return resultMap;
  }
}
