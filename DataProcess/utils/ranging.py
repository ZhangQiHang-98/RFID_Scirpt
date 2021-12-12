# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：ranging.py
@Author     ：Zhang Qihang
@Description: 论文中使用到的工具类
@Date       ：2021/11/23 19:55 
"""
import matplotlib.pyplot as plt
import pandas as pd
import glob
import os
import math
import myunwrap
from config import *
from filters import *
import numpy as np
from scipy import constants as C
import re
from scipy.optimize import leastsq
from scipy.stats import kstest


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
    freq_list = []
    phase_list = []
    ideal_list = []
    alpha_list = []

    # 1. 滤波并且计算平均相位（测量值）与理论相位
    for name, group in grouped:
        freq_list.append(name)
        phase_list.append(2 * C.pi - np.mean(hampel(group["phase"].values)))
        ideal_list.append((4 * C.pi * distance * name * 1e6 / C.c) % (2 * C.pi))
    # 2. 进行unwrap处理
    phase_list = myunwrap.unwrap(phase_list)
    ideal_list = myunwrap.unwrap(ideal_list)
    # 2.1. 绘制未做任何处理的相位拟合直线图
    x = np.array(freq_list)
    y_ideal = np.array(ideal_list)
    y = np.array(phase_list)
    calc_slope(x, y, y_ideal)

    # 计算alpha值
    for i in range(len(freq_list)):
        ideal_value = ideal_list[i]
        # 如果测量值大于理论值，则说明理论值和测量值还在一个周期内，否则应该让测量值+2PI
        if phase_list[i] > ideal_value:
            alpha_list.append(phase_list[i] - ideal_value)
        else:
            if math.fabs((phase_list[i] - ideal_value)) < 0.2:
                alpha_list.append(math.fabs((phase_list[i] - ideal_value)))
            else:
                alpha_list.append(phase_list[i] + 2 * C.pi - ideal_value)
    # 输出alpha值
    return alpha_list
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
    u = np.mean(y)
    std = np.std(y)
    low = u - 3 * std
    high = u + 3 * std
    for item in y[:]:
        if item > high or item < low:
            index = int(np.argwhere(y == item))
            x = np.delete(x,index)
            y = np.delete(y,index)
    calc_slope(x, y, [])


def lhk(file_path):
    # 取10-15六组数据，此六组数据还为超过一个波长，因此理论相位都不大于2PI
    distance = float(re.findall(r"\d+\.?\d*", path)[0]) / 100
    df = pd.read_csv(path, header=None)
    df.columns = ["tag", "freq", "timestamp", "phase", "rss"]
    grouped = df.groupby("freq")
    freq_list = []
    phase_list = []
    ideal_list = []
    alpha_list = []

    # 1. 滤波并且计算平均相位（测量值）与理论相位
    for name, group in grouped:
        freq_list.append(name)
        phase_list.append(2 * C.pi - np.mean(hampel(group["phase"].values)))
        ideal_list.append((4 * C.pi * distance * name * 1e6 / C.c) % (2 * C.pi))
    # 2. 进行unwrap处理
    # phase_list = myunwrap.unwrap(phase_list)
    ideal_list = myunwrap.unwrap(ideal_list)
    diff = phase_list[-1] - phase_list[0]
    return phase_list


# todo 进行unwrap操作
if __name__ == "__main__":
    alpha_path = glob.glob(os.path.join(ALPHA_PATH, '*.csv'))
    test_path = glob.glob(os.path.join(TEST_PATH, '*.csv'))
    # result = []
    # x = []
    # y1 = []
    # y6 = []
    # y10 = []
    # y15 = []
    # for path in alpha_path:
    #     x.append(float(re.findall(r"\d+\.?\d*", path)[0]) / 100)
    #     result.append(lhk(path))
    # for i in range(len(result)):
    #     y1.append(result[i][1])
    #     y6.append(result[i][6])
    #     y10.append(result[i][10])
    #     y15.append(result[i][15])
    # y1 = myunwrap.unwrap(y1)
    # y6 = myunwrap.unwrap(y6)
    # y10 = myunwrap.unwrap(y10)
    # y15 = myunwrap.unwrap(y15)
    # plt.plot(x, y1, label="y1")
    # plt.plot(x, y6, label="y6")
    # plt.plot(x, y10, label="y10")
    # # plt.plot(x, y15, label="y15")
    # plt.legend()
    # plt.show()
    #
    # plt.plot(x, (np.array(y10) - np.array(y1)) / 9, label="y10-y1")
    # plt.plot(x, (np.array(y6) - np.array(y1)) / 5, label="y6-y1")
    # # plt.plot(x, (np.array(y15) - np.array(y6)) / 9, label="y15-y6")
    # plt.legend()
    # plt.show()
    #
    # for path in rp_path:
    #     data_process.rp_process(path)
    # result_dict = {}
    total_diff = np.zeros(16)
    file_len = len(alpha_path)
    # # 首先计算阿尔法值
    for path in alpha_path:
        print(path)
        # distance = float(re.findall(r"\d+\.?\d*", path)[0]) / 100
        alpha_list = calc_alpha(path)
        print(alpha_list)
        total_diff += np.array(alpha_list)
    total_diff = total_diff / file_len
    print("calc_alpha", total_diff)
    for path in test_path:
        clear_alpha(total_diff, path)

# 最终取到的平均插值
# for path in test_path:
#     calc_slope(mean_diff, path)
