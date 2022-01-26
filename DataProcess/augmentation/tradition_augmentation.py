#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@File    ：tradition_augmentation.py
@Author  ：Zhang Qihang
@Date    ：2022/1/10 19:17 
@Description : 传统的时序数据增强算法，根据2017年的ICMI整理而来
"""
import numpy as np
from scipy import interpolate
from scipy.interpolate import CubicSpline  # for warping
from transforms3d.axangles import axangle2mat  # for rotation
from random import choice


# 1.添加噪声，默认为高斯噪声（正态分布），sigma表示噪声的标准差
def add_noise(X, sigma=0.05):
    myNoise = np.random.normal(loc=0, scale=sigma, size=X.shape)
    return X + myNoise


# 2.放缩(Scaling)，序列整体纵向拉伸或收缩，sigma表示放缩因子的标准差
def scaling(X, sigma=0.1):
    scalingFactor = np.random.normal(loc=1.0, scale=sigma, size=(1, X.shape[1]))
    myNoise = np.matmul(np.ones((X.shape[0], 1)), scalingFactor)
    return X * myNoise


# Mag与Time Wrap的工具函数，用于生成具备三次函数的随机曲线，knot代表随机选择几个点当作插值锚点
# cs_x,cs_y,cs_z为生成的插值曲线
def GenerateRandomCurves(X, sigma=0.2, knot=4):
    # 先reshape，从(100,)变成(100,1)这种
    xx = (np.ones((X.shape[1], 1)) * (np.arange(0, X.shape[0], (X.shape[0] - 1) / (knot + 1)))).transpose()
    yy = np.random.normal(loc=1.0, scale=sigma, size=(knot + 2, X.shape[1]))
    x_range = np.arange(X.shape[0])
    cs = CubicSpline(xx[:, 0], yy[:, 0])
    return np.array([cs(x_range)]).transpose()


# 3. 幅度卷曲(Magnitude Warping, MagW)，理解为每个值都有不同程度的在1左右的放缩，在y轴上卷曲
def mag_warp(X, sigma=0.2, knot=4):
    X = np.reshape(X, (X.shape[0], 1))
    return X * GenerateRandomCurves(X, sigma, knot)


# Time Warping的工具函数，返回的是扭曲后的节点位置，(l,m)的矩阵
def distort_time_steps(X, sigma=0.2):
    # tt对应MagWrap中的扭曲曲线
    tt = GenerateRandomCurves(X, sigma)  # Regard these samples aroun 1 as time intervals
    tt_cum = np.cumsum(tt, axis=0)  # Add intervals to make a cumulative graph
    # Make the last value to have X.shape[0]
    t_scale = [(X.shape[0] - 1) / tt_cum[-1, 0]]
    # 每个位置
    tt_cum[:, 0] = tt_cum[:, 0] * t_scale[0]

    return tt_cum


# 4. 时间卷曲(Time Warping)，在x轴上卷曲，将每个值往前，往后进行放缩
def time_warp(X, sigma=0.2):
    tt_new = distort_time_steps(X, sigma)
    X_new = np.zeros(X.shape)
    x_range = np.arange(X.shape[0])
    # 一维插值 参数分别表示:待插入数据的横坐标，原始数据的横坐标，原始数据的纵坐标
    X_new[:, 0] = np.interp(x_range, tt_new[:, 0], X[:, 0])
    return X_new


# 5. 简单的进行旋转，但感觉在手势识别这里应该不太好用
def rotation(X):
    axis = np.random.uniform(low=-1, high=1, size=X.shape[1])
    angle = np.random.uniform(low=-np.pi, high=np.pi)
    return np.matmul(X, axangle2mat(axis, angle))


# 6. 排列，切成n段，重新进行排列
def permutation(X, nPerm=4, minSegLength=10):
    X_new = np.zeros(X.shape)
    idx = np.random.permutation(nPerm)
    bWhile = True
    while bWhile:
        segs = np.zeros(nPerm + 1, dtype=int)
        segs[1:-1] = np.sort(np.random.randint(minSegLength, X.shape[0] - minSegLength, nPerm - 1))
        segs[-1] = X.shape[0]
        if np.min(segs[1:] - segs[0:-1]) > minSegLength:
            bWhile = False
    pp = 0
    for ii in range(nPerm):
        x_temp = X[segs[idx[ii]]:segs[idx[ii] + 1], :]
        X_new[pp:pp + len(x_temp), :] = x_temp
        pp += len(x_temp)
    return X_new


# 7. 随机采样，随机选择百分之多少的点，根据这些点进行插值然后还原
def rand_sample_time_steps(X, percent=0.35):
    nSample = X.shape[0] * percent
    X_new = np.zeros(X.shape)
    tt = np.zeros((nSample, X.shape[1]), dtype=int)
    tt[1:-1, 0] = np.sort(np.random.randint(1, X.shape[0] - 1, nSample - 2))
    tt[1:-1, 1] = np.sort(np.random.randint(1, X.shape[0] - 1, nSample - 2))
    tt[1:-1, 2] = np.sort(np.random.randint(1, X.shape[0] - 1, nSample - 2))
    tt[-1, :] = X.shape[0] - 1
    return tt


# 8. 窗口切片，默认值是0.8，也就是取整个序列的80%
def window_slicing(X, percent=0.8):
    # 一共切n片
    n = int((1 - percent) / 0.05) + 1
    sublen = int(X.shape[0] * percent)
    res = []
    for i in range(0, n):
        start = int(i * 0.05 * X.shape[0])
        end = start + sublen
        if end >= X.shape[0]:
            end = X.shape[0]
        res.append(X[start:end])
    # 把原来的序列也返回
    res.append(X)
    return res


# 9.窗口规整，取整个序列的某一个部分进行窗口规整
# 随机选择10%的序列进行规整，在这里规定好不选择前10%或者是后10%，感觉误差会比较大，因为最前最后还是有未开始的地方
# 后面如果调整好了，可以选择前10或者后10
def window_wraping(X, percent=0.1):
    res = []
    choices = [0, 1]
    for i in range(1, 9):
        # start和end是指子序列的开始与结尾
        start = int(i * percent * X.shape[0])
        end = int(start + percent * X.shape[0])
        sub = X[start:end + 1]

        x = np.arange(start, end + 1, 1)
        f = interpolate.interp1d(x, sub, kind="cubic")
        # 随机选择加速或者减速
        if choice(choices):
            x_new = np.arange(start, end, 0.5)
        else:
            x_new = np.arange(start, end, 2)
        y_new = f(x_new)
        res.append(np.concatenate((X[0:start], y_new, X[end:]), axis=0))
    return res


def DA_RandSampling(X, percent=0.35):
    tt = rand_sample_time_steps(X, percent)
    X_new = np.zeros(X.shape)
    X_new[:, 0] = np.interp(np.arange(X.shape[0]), tt[:, 0], X[tt[:, 0], 0])
    X_new[:, 1] = np.interp(np.arange(X.shape[0]), tt[:, 1], X[tt[:, 1], 1])
    X_new[:, 2] = np.interp(np.arange(X.shape[0]), tt[:, 2], X[tt[:, 2], 2])
    return X_new
