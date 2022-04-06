# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess
@File       ：aug_utils.py
@Author     ：Zhang Qihang
@Description: 数据增强过程中的一些工具函数
@Date       ：2022/1/18 21:09
"""
import pandas as pd
from scipy import interpolate
import numpy as np
import math


# 插值函数
def my_interpolation(df, kind="cubic"):
    # 计算时间间隔
    start_time = df['time'].values[0]
    end_time = df['time'].values[-1]
    time_interval = int(end_time - start_time) / 1000000
    x = (df['time'].values - start_time) / 1000000
    y = df['phase'].values
    # 取到最后一个值
    # 根据时间长短进行二次插值
    x_new = np.linspace(0, x[-1], num=int(time_interval) * 100)
    f = interpolate.interp1d(x, y, kind=kind)
    y_new = f(x_new)
    return y_new


# 生成的所有数据进行存储的函数
def my_write_file(x_train, y_train, synthetic_x_train, synthetic_y_train):
    # 处理synthetic_x_train的形状
    total_x_train = list(x_train)
    total_y_train = list(y_train) + list(synthetic_y_train)
    for item in synthetic_x_train:
        item = np.reshape(item, (item.shape[0],))
        total_x_train.append(item)

    # 计算长度
    total_len = 0
    for item in total_x_train:
        total_len += len(item)
    # 拉成等长且下采样后的值
    avg_len = int(total_len / len(total_x_train) / 10)
    # 分别处理原有的和生成的训练数据
    final_x = []
    for item in total_x_train:
        # 原本长度为item.shape[0]，后来更新成了平均长度
        x = np.linspace(0, item.shape[0], num=item.shape[0])
        x_new = np.linspace(0, item.shape[0], num=avg_len)
        f = interpolate.interp1d(x, item, kind='cubic')
        after_inter = f(x_new)
        final_x.append(list(after_inter))

    # 将x与y拼接好，放入df文件中即可

    res = []
    for i in range(len(final_x)):
        temp = final_x[i]
        temp.append(total_y_train[i])
        res.append(temp)
    df = pd.DataFrame(res)

    df.to_csv("init_data.csv", index=False, header=None)
