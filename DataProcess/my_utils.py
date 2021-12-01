# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：my_utils.py
@Author     ：Zhang Qihang
@Description: 论文中使用到的工具类
@Date       ：2021/11/23 19:55 
"""
import matplotlib.pyplot as plt
import pandas as pd
import glob
import os

import myunwrap
from config import *
from filters import *
import numpy as np
from scipy import constants as C
import re
from scipy.optimize import leastsq


# 利用scipy中的leastsq来计算斜率
def func(p, x):
    k, b = p
    return k * x + b


def error(p, x, y):
    return func(p, x) - y  # x、y都是列表，故返回值也是个列表


# 由于相位数据的特殊性，传值的时候，只传入x也可
def calc_slope(x, y, y_ideal):
    # 如果y为空的话
    p0 = [1, 1]
    para = leastsq(error, p0, args=(x, y))
    k, b = para[0]
    # d为根据斜率推算的距离
    d = k * C.c / (4 * C.pi * 1e6)
    print(k, b, d)

    plt.figure(figsize=(8, 6))
    plt.scatter(x, y, color="red", label="original data", linewidth=3)  # 画样本点
    if len(y_ideal) > 0:
        plt.scatter(x, y_ideal, color="blue", label="ideal data", linewidths=3.5)  # 画标准值
    x = np.linspace(x[0], x[-1], 1000)
    y = k * x + b
    plt.plot(x, y, color="orange", label="Fitting Line", linewidth=2)  # 画拟合直线
    plt.legend()
    plt.show()


def calc_alpha(path):
    # 取10-15六组数据，此六组数据还为超过一个波长，因此理论相位都不大于2PI
    distance = float(re.findall(r"\d+\.?\d*", path)[0]) / 100
    df = pd.read_csv(path, header=None)
    df.columns = ["tag", "freq", "timestamp", "phase", "rss"]
    grouped = df.groupby("freq")
    phase_dict = {}
    alpha_dict = {}
    ideal_dict = {}

    # 1. 滤波并且计算平均相位（测量值）与理论相位
    for name, group in grouped:
        phase_dict[name] = 2 * C.pi - np.mean(hampel(group["phase"].values))
        ideal_dict[name] = (4 * C.pi * distance * name * 1e6 / C.c) % (2 * C.pi)
    # 2. 绘制未做任何处理的相位拟合直线图
    x = np.array(list(phase_dict.keys()))
    y_ideal = np.array(list(ideal_dict.values()))
    y = np.array(list(phase_dict.values()))
    calc_slope(x, y, y_ideal)

    # 计算alpha值
    for key in phase_dict.keys():
        ideal_value = ideal_dict[key]
        # 如果测量值大于理论值，则说明理论值和测量值还在一个周期内，否则应该让测量值+2PI
        if phase_dict[key] > ideal_value:
            alpha_dict[key] = phase_dict[key] - ideal_value
        else:
            alpha_dict[key] = 2 * C.pi + phase_dict[key] - ideal_value
    # 输出alpha值
    return alpha_dict
    # 计算插值
    # diff_dict = {}
    # start_freq = 920.625
    # for key in alpha_dict.keys():
    #     diff_dict[key] = alpha_dict[key] - alpha_dict[920.625]


# 消除α的影响，传入阿尔法值，进行删除。
def clear_alpha(alpha_list, path):
    df = pd.read_csv(path, header=None)
    df.columns = ["tag", "freq", "timestamp", "phase", "rss"]
    grouped = df.groupby("freq")
    phase_dict = {}
    for name, group in grouped:
        phase_dict[name] = 2 * C.pi - np.mean(hampel(group["phase"].values))

    x = np.array(list(phase_dict.keys()))
    y = np.array(myunwrap.unwrap(list(phase_dict.values()))) - alpha_list
    print(y)
    calc_slope(x, y, [])


if __name__ == "__main__":
    alpha_path = glob.glob(os.path.join(ALPHA_PATH, '*.csv'))
    test_path = glob.glob(os.path.join(TEST_PATH, '*.csv'))
    # for path in rp_path:
    #     data_process.rp_process(path)
    result_dict = {}
    total_diff = np.zeros(16)
    file_len = len(alpha_path)
    # 首先计算阿尔法值
    for path in alpha_path:
        print(path)
        # distance = float(re.findall(r"\d+\.?\d*", path)[0]) / 100
        result_dict = calc_alpha(path)
        total_diff += np.array(list(result_dict.values()))
    total_diff = total_diff / file_len
    for path in test_path:
        clear_alpha(total_diff, path)
# 最终取到的平均插值
# for path in test_path:
#     calc_slope(mean_diff, path)
