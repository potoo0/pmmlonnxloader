import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression

from sklearn2pmml.pipeline import PMMLPipeline
from sklearn2pmml import sklearn2pmml

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


# build model pipeline
pipeline_lr = PMMLPipeline([
    ('LinearRegressor', LinearRegression()),
])


# train
pipeline_lr.fit(X_train, y_train)


# predict
y_hat = pipeline_lr.predict(X_test)
loss = y_hat == y_test
accuracy = np.mean(loss)
print(
    f'model accuracy: {round(accuracy, 4)},\n',
    f'predict res: {pipeline_lr.predict(X_test[0:2])},\n',
    f'groundtruth: {y_test[0:2]}'
)


# save
pmml_model_path = os.path.join(export_dir, 'a_multiply_b_add_c.pmml')
sklearn2pmml(pipeline_lr, pmml_model_path, with_repr=True)
