import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression

from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

import os


export_dir = '../models'

# faked Data
n_samples = 200
n_feature = 3
np.random.seed(10)
X = np.random.randint(low=-100, high=100, size=(n_samples, n_feature))
y = np.multiply(X[:, 0], X[:, 1]) + X[:, 2]

X_train, X_test, y_train, y_test = train_test_split(
    X, y,
    test_size=0.3, shuffle=False)


# build model
model_lr = LinearRegression()


# train
model_lr.fit(X_train, y_train)


# predict
y_hat = model_lr.predict(X_test)
loss = y_hat == y_test
accuracy = np.mean(loss)
print(
    f'model accuracy: {round(accuracy, 4)},\n',
    f'predict res: {model_lr.predict(X_test[0:2])},\n',
    f'groundtruth: {y_test[0:2]}'
)


# model export to onnx
onnx_model_path = os.path.join(export_dir, 'a_multiply_b_add_c.onnx')
initial_type = [('float_input', FloatTensorType([1, X_train.shape[1]]))]
onx = convert_sklearn(model_lr, initial_types=initial_type)
with open(onnx_model_path, "wb") as f:
    f.write(onx.SerializeToString())
