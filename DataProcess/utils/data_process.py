2  # !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project ：DataProcess
@File    ：data_process.py
@Author  ：Zhang Qihang
@Date    ：2021/11/8 13:18
https://cloud.tencent.com/developer/article/1650150
"""
import pandas as pd
import numpy as np
import myplot
import myunwrap
import math


def rp_process(file_path):
    # 获取csv中的数据，默认没有频率不一致的情况
    df = pd.read_csv(file_path)
    df.columns = ["tag", "cur_freq", "cur_power", "real_freq", "time_stamp", "phase"]
    # TODO : 判断是否有频率不一致的情况
    # TODO : 判断是否有read_rate为0的情况

    # 遍历整个df，记录每个频率和功率下的平均阅读率和平均相位,SAC过程（split-apply-combine），生成特征矩阵
    grouped = df.groupby(["cur_freq", "cur_power"])
    rate_mat = []
    phase_mat = []
    rate_row = []
    phase_row = []
    cur_freq = df.iloc[0]["cur_freq"]
    for name, group in grouped:
        # 计算当前频率和功率下的平均阅读率
        first_time = group.iloc[0]["time_stamp"]
        last_time = group.iloc[-1]["time_stamp"]
        read_rate = len(group) * 1000000 / (last_time - first_time)
        # 当前频率和功率下的平均相位
        avg_phase = group["phase"].mean()
        print(name, read_rate, avg_phase)
        if cur_freq != name[0]:
            rate_mat.append(rate_row)
            phase_mat.append(phase_row)
            rate_row = []
            phase_row = []
            cur_freq = name[0]
        rate_row.append(read_rate)
        phase_row.append(avg_phase)
    # 将最后一个频率的特征添加到特征矩阵中
    rate_mat.append(rate_row)
    phase_mat.append(phase_row)

    rate_mat = np.array(rate_mat)
    phase_mat = np.array(phase_mat)

    # 获取MTP矩阵，取每个频率下，最小可以阅读到的功率等级（在这里暂时用index代替）
    mtp_vec = []
    for i in range(rate_mat.shape[0]):
        for j in range(rate_mat.shape[1]):
            if rate_mat[i][j] != 0:
                mtp_vec.append(j)
                break
            continue
    # 获取ITPA矩阵

    # 对相位进行处理，首先绘制相位热力图
    # 转置
    myplot.phase_heatmap(phase_mat)


def hop_process(file_path):
    df = pd.read_csv(file_path)
    df.columns = ["tag", "freq", "time_stamp", "phase", "rssi"]
    phase_list = 2 * math.pi - df["phase"].values
    phase_list = myunwrap.unwrap(phase_list)

    # 暂时只展示相位图
    myplot.phase_scatter(phase_list)
