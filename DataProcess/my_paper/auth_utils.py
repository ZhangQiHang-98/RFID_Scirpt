# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：auth_utils.py
@Author     ：Zhang Qihang
@Description: 与认证阶段以及认证矩阵有关的函数
@Date       ：2021/12/23 14:44 
"""
import pandas as pd
from config import *
import numpy as np
import scipy.constants as C
from filters import *
from myunwrap import *


# 计算参考矩阵
def calc_refer_mat(auth_df_path):
    df = pd.read_csv(auth_df_path, header=None)
    df.columns = MORE_COLUMNS
    grouped = df.groupby(['real_freq', 'real_power'])
    # 此处的phase_mat[i][j] i为频率索引，j为功率索引
    phase_mat = []
    phase_row = []
    cur_freq = df.iloc[0]["real_freq"]
    for name, group in grouped:
        cur_phase = 2 * C.pi - np.mean(hampel(group["phase"].values))
        if cur_freq != name[0]:
            phase_mat.append(phase_row)
            phase_row = []
            cur_freq = name[0]
        phase_row.append(cur_phase)
    phase_mat.append(phase_row)
    phase_mat = np.array(phase_mat)

    # 对行和列分别进行unwrapper操作
    for i in range(phase_mat.shape[1]):
        phase_mat[:, i] = unwrap(phase_mat[:, i])
    for i in range(phase_mat.shape[0]):
        phase_mat[i, :] = unwrap(phase_mat[i, :])
    return phase_mat
