# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：aug_utils.py
@Author     ：Zhang Qihang
@Description: 数据增强过程中的一些工具函数
@Date       ：2022/1/18 21:09 
"""
from scipy import interpolate
import numpy as np


def my_interpolation(df, kind="linear"):
    # 计算时间间隔
    start_time = df['time'].values[0]
    end_time = df['time'].values[-1]
    time_interval = int(end_time - start_time) / 1000000
    x = df['time'].values / 1000000
    y = df['phase'].values
    # 根据时间长短进行二次插值
    x_new = np.linspace(0, time_interval)
    f = interpolate.interp1d(x, y, kind=kind)
    y_new = f(x_new)
    return y_new