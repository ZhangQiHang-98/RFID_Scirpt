# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：play.py
@Author     ：Zhang Qihang
@Description: 随便测测
@Date       ：2021/12/12 13:09 
"""
# os.walk()的使用
from dba import calculate_dist_matrix
import utils.constants
import numpy as np

x = np.array([1, 1, 3, 3, 2, 4], dtype=float)
x = x.reshape(x.shape[0], 1)
y = np.array([1, 3, 2, 2, 4, 4], dtype=float)
y = y.reshape(y.shape[0], 1)
z = [x, y]
z = np.array(z)

dist_fun = utils.constants.DISTANCE_ALGORITHMS['dtw']
dist_fun_params = utils.constants.DISTANCE_ALGORITHMS_PARAMS['dtw']
print(calculate_dist_matrix(z, dist_fun, dist_fun_params))


a = [1,2]
a.append()