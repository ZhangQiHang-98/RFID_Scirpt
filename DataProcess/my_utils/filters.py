#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project ：DataProcess Hampel滤波函数
@File    ：filters.py
@Author  ：Zhang Qihang
@Date    ：2021/11/14 18:35 
"""
import numpy as np
import config
from tsmoothie.smoother import *
from pykalman import KalmanFilter
import myunwrap


# 总体过滤函数
def do_filter(df):
    # 继续对生成的时间序列进行滤波处理
    df["phase"] = np.array(myunwrap.unwrap(df["phase"].values))
    df["phase"] = lowess(df["phase"])
    # processed_df["phase"] = filters.lowess(processed_df["phase"])
    # processed_df["phase"] = filters.kalman(processed_df["phase"])
    # processed_df["phase"] = filters.Kalman1D(processed_df["phase"])
    return df


# 该滤波器用来去除异常点
def hampel(X):
    length = X.shape[0] - 1
    k = config.HAMPEL
    nsigma = 3
    iLo = np.array([i - k for i in range(0, length + 1)])
    iHi = np.array([i + k for i in range(0, length + 1)])
    iLo[iLo < 0] = 0
    iHi[iHi > length] = length
    xmad = []
    xmedian = []
    for i in range(length + 1):
        w = X[iLo[i]:iHi[i] + 1]
        medj = np.median(w)
        mad = np.median(np.abs(w - medj))
        xmad.append(mad)
        xmedian.append(medj)
    xmad = np.array(xmad)
    xmedian = np.array(xmedian)
    scale = 1.4826  # 缩放
    xsigma = scale * xmad
    xi = ~(np.abs(X - xmedian) <= nsigma * xsigma)  # 找出离群点（即超过n sigma个标准差）

    # 将离群点替换为中位数值
    xf = X.copy()
    xf[xi] = xmedian[xi]
    return np.array(xf)


# tsmoothie中的卡尔曼滤波
def kalman(phases):
    smoother = KalmanSmoother(component='level_trend',
                              component_noise={'level': 0.1, 'trend': 0.3})
    smoother.smooth(phases)
    return smoother.smooth_data[0]


# tsmoothie中的lowess滤波函数，目前还是这个的效果比较好，锯齿也比较少，后面再看
def lowess(phases, window_shape=20, alpha=0.1):
    smoother = LowessSmoother(smooth_fraction=alpha, iterations=1)
    smoother.smooth(phases)
    return smoother.smooth_data[0]


# 利用pyKalman库实现的卡尔曼滤波
def Kalman1D(observations, damping=1):
    # To return the smoothed time series data
    observation_covariance = damping
    initial_value_guess = observations[0]
    transition_matrix = 1
    transition_covariance = 0.1
    initial_value_guess
    kf = KalmanFilter(
        initial_state_mean=initial_value_guess,
        initial_state_covariance=observation_covariance,
        observation_covariance=observation_covariance,
        transition_covariance=transition_covariance,
        transition_matrices=transition_matrix
    )
    pred_state, state_cov = kf.smooth(observations)
    return pred_state
