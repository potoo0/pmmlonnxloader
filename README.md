java load onnx and pmml model demo, serving by RESTful api with spring-boot.

# api usage

## 1. load onnx and inference

**Request**

- Method: **POST**

- URL: `/onnx/json`

- Body demo1:

  ```json
  {
    "X": [
      {
        "float_input": [[5, 4.4, 1.5]]
      }
    ],
    "modelPath": "/models/a_multiply_b_add_c.onnx"
  }
  ```

- Body demo2:

  ```json
  {
    "X": [
      {
        "float_input": [[5, 4.4, 1.5]]
      }
    ],
    "modelPath": "ftp://ftp1:12@localhost/mlflowModel/9/1/a_multiply_b_add_c.onnx"
  }
  ```

**Response**

- Body demo:

  ```json
  {
    "msg": "ok",
    "code": "200",
    "data":{
      "variable":[[41.637947]]
    },
    "modelPath": "/models/a_multiply_b_add_c.onnx"
  }
  ```

## 2. load pmml and inference

**Request**

- Method: **POST**

- URL: `/pmml/json`

- Body demo1:

  ```json
  {
    "X": [
      {
        "x1": 5.7,
        "x2": 4.4,
        "x3": 1.5
      }
    ],
    "modelPath": "/models/a_multiply_b_add_c.pmml"
  }
  ```

- Body demo2:

  ```json
  {
    "X": [
      {
        "x1": 5.7,
        "x2": 4.4,
        "x3": 1.5
      }
    ],
    "modelPath": "ftp://ftp1:12@localhost/mlflowModel/9/1/a_multiply_b_add_c.pmml"
  }
  ```

**Response**

- Body demo:

  ```json
  {
    "msg": "ok",
    "modelName": "models\\a_multiply_b_add_c.pmml",
    "code": "200",
    "data":{
    	"y": 42.67686896677073
    }
  }
  ```