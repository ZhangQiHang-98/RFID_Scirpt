# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess
@File       ：reproduce_gyro.py
@Author     ：Zhang Qihang
@Description: 复现Gyro论文中的消除跳频方法
@Date       ：2021/12/21 16:07
"""
import numpy as np
import scipy.constants as C
import filters
from config import *
from myplot import *


def gyro(refer_mat, init_df):
    """

    Args:
        refer_mat: 参考的相位矩阵
        init_df: 未处理的原始dataframe

    Returns:
        process_df：处理后的dataframe
    """
    refer_mat = np.array(refer_mat)
    # 首先对df进行相位处理（2pi-和hampel）
    init_df["phase"] = 2 * C.pi - init_df["phase"]
    refer_freq_index = get_freq_index(920.625)
    refer_power_index = get_power_index(25)
    # 遍历init_df
    refer_phases = []
    for i in range(init_df.shape[0]):
        # 分别计算当前功率和频率在参考矩阵中的下标
        cur_freq = init_df.iloc[i]["real_freq"]
        freq_index = get_freq_index(cur_freq)
        cur_power = init_df.iloc[i]["real_power"]
        power_index = get_power_index(cur_power)
        refer_phase = init_df.iloc[i]["phase"] - refer_mat[freq_index][power_index]  # 第一步，对应1-2
        refer_phase = refer_phase * REFER_CHANNEL / cur_freq  # 第二步，对应×频率比值
        refer_phase = refer_phase + refer_mat[refer_freq_index][refer_power_index]
        # 将当前频率、功率下的相位转移到参考相位中
        refer_phases.append(refer_phase)
    processed_df = init_df.copy(deep=True)
    processed_df["phase"] = filters.hampel(np.array(refer_phases))
    # 分别绘制原始的相位图以及处理过后的相位图
    phase_scatter(init_df)
    phase_scatter(processed_df)
    return processed_df
