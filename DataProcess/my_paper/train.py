#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@File    ：train.py
@Author  ：Zhang Qihang
@Date    ：2022/1/28 14:43 
@Description : 进行数据的分类测试
"""
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split

from net.resnet import Classifier_RESNET

# 首先划分训练集和测试集
df = pd.read_csv("../dataset/all_factor_data/init_data.csv", header=None)
x = df.iloc[:, 0:-2]
y = df.iloc[:, -1]
x = np.array(x)
y = np.array(y)
x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.25, random_state=0)
# 计算类别数目
nb_classes = len(np.unique(np.concatenate((y_train, y_test), axis=0)))
# 真实类别
classes, classes_counts = np.unique(y_train, return_counts=True)
max_prototypes = min(classes_counts.max() + 1, 5 + 1)
nb_prototypes = classes_counts.max()
if len(x_train.shape) == 2:  # if univariate
    # add a dimension to make it multivariate with one dimension
    x_train = x_train.reshape((x_train.shape[0], x_train.shape[1], 1))
    x_test = x_test.reshape((x_test.shape[0], x_test.shape[1], 1))
    # 变成(num,l,m)  l为一个的长度，m为维度，这里m统一为1
print(nb_classes, classes, classes_counts.max())

classifier = Classifier_RESNET("../models", x_train.shape[1:], nb_classes, nb_prototypes, classes, verbose=True)
y_pred = classifier.fit(x_train, y_train, x_test, y_test)
