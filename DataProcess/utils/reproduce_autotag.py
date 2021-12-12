from typing import Dict

# !/usr/bin/env python
# -*- coding: UTF-8 -*-


"""
@Project ：DataProcess
@File    ：reproduce_autotag.py
@Author  ：Zhang Qihang
@Date    ：2021/11/8 13:18
https://cloud.tencent.com/developer/article/1650150
"""

import pandas as pd
import numpy as np
import myplot
import myunwrap
import math
import filters
import config

freq_phase_dict = {}


# TODO : 对AutoTag的处理方式进行适当的修改
# 由于不同相位的alpha*f1/fi为一个定值，能否直接将其计算出来
def auto_tag_process(df):
    # 首先根据df中的频率进行分组
    # 抽取一个字典

    for i in range(len(df)):
        if df.iloc[i]["freq"] not in freq_phase_dict:
            freq_phase_dict[df.iloc[i]["freq"]] = {}
            freq_phase_dict[df.iloc[i]["freq"]]["data"] = []
            freq_phase_dict[df.iloc[i]["freq"]]["data"].append(df.iloc[i]["phase"])
        freq_phase_dict[df.iloc[i]["freq"]]["data"].append(df.iloc[i]["phase"])
    # 计算所有频率前6个与参考频率后6个的平均相位，传回字典
    for freq in freq_phase_dict.keys():
        freq_phase_dict[freq]["mean_phase"] = np.mean(freq_phase_dict[freq]["data"][-6:])
    # 计算所有的参数值
    pre_channel = ""
    for freq in freq_phase_dict.keys():
        if freq == config.REFER_CHANNEL:
            pre_channel = freq
            continue
        # 计算前一个频率，转换为f1时的相位
        freq_phase_dict[freq]["factor"] = trans_phase(pre_channel) - config.REFER_CHANNEL / freq * \
                                          freq_phase_dict[freq]["mean_phase"]
        pre_channel = freq

    # 修改df，将所有频率相位映射到参考频率中
    new_phase = []
    for i in range(len(df)):
        cur_freq = df.iloc[i]["freq"]
        if cur_freq == config.REFER_CHANNEL:
            new_phase.append(df.iloc[i]["phase"])
            continue
        new_phase.append(df.iloc[i]["phase"] * config.REFER_CHANNEL / cur_freq + freq_phase_dict[cur_freq]["factor"])
    df["phase"] = new_phase
    return df


# 将上一个频率的相位map到f1信道上
def trans_phase(pre_channel):
    # 如果当前为第二信道,直接返回即可
    if pre_channel == config.REFER_CHANNEL:
        return freq_phase_dict[pre_channel]["mean_phase"]
    res = freq_phase_dict[pre_channel]["mean_phase"] * config.REFER_CHANNEL / pre_channel + \
          freq_phase_dict[pre_channel]["factor"]
    return res


def hop_process(file_path):
    df = pd.read_csv(file_path)
    df.columns = ["tag", "freq", "time_stamp", "phase", "rssi"]
    # 相位图的横坐标
    x_list = df["time_stamp"].values - df["time_stamp"].values[0]
    phase_list = 2 * math.pi - df["phase"].values

    # 将相位进行Unwrap操作
    phase_list = myunwrap.unwrap(phase_list)
    phase_list = filters.hampel(np.array(phase_list))
    df["phase"] = phase_list
    # 初始相位
    myplot.phase_scatter(x_list, df["phase"])
    # AutoTag文中，消除跳频的影响
    df = auto_tag_process(df)
    # 消除跳频影响之后的相位
    myplot.phase_scatter(x_list, df["phase"])
