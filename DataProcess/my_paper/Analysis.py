# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：Analysis.py
@Author     ：Zhang Qihang
@Description: 对数据进行分析
@Date       ：2021/12/13 14:48 
"""
import glob
import os
from config import *
import pandas as pd
from scipy import constants as C
from filters import *
import myunwrap
import matplotlib.pyplot as plt


def plot_relation(phase_mat, powers, freqs):
    # 绘制子图
    fig = plt.figure(figsize=(10, 5))
    ax1, ax2 = fig.subplots(1, 2)
    for i in range(phase_mat.shape[0]):
        ax1.plot(powers, phase_mat[i, :], label=str(freqs[i]))
        ax1.legend()
    for i in range(phase_mat.shape[1]):
        ax2.plot(freqs, phase_mat[:, i], label=str(powers[i]))
    plt.legend()
    plt.show()


# 探究功率与频率对相位的关系
def relation_both_with_phase(path):
    df = pd.read_csv(path, header=None)
    df.columns = MORE_COLUMNS
    grouped = df.groupby(['real_freq', 'real_power'])

    phase_mat = []
    phase_row = []
    cur_freq = df.iloc[0]["real_freq"]
    for name, group in grouped:
        print(name)
        cur_phase = 2 * C.pi - np.mean(hampel(group["phase"].values))
        if cur_freq != name[0]:
            phase_mat.append(phase_row)
            phase_row = []
            cur_freq = name[0]
        phase_row.append(cur_phase)
    phase_mat.append(phase_row)
    phase_mat = np.array(phase_mat)
    # 先按照列进行unwrap操作
    for i in range(phase_mat.shape[1]):
        phase_mat[:, i] = myunwrap.unwrap(phase_mat[:, i])
    # 再按照行进行unwrap操作
    for i in range(phase_mat.shape[0]):
        phase_mat[i, :] = myunwrap.unwrap(phase_mat[i, :])

    # 横坐标为功率，纵坐标为相位
    power_list = get_power_list(15, 30)
    freq_list = get_freq_list(920.625, 924.125)
    plot_relation(phase_mat, power_list, freq_list)


if __name__ == '__main__':
    auth_datas = glob.glob(os.path.join(AUTH_PATH, '*.csv'))
    for auth_data in auth_datas:
        relation_both_with_phase(auth_data)
