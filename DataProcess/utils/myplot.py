#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project ：DataProcess 
@File    ：myplot.py
@Author  ：Zhang Qihang
@Date    ：2021/11/8 14:41 
"""
import seaborn as sns
import matplotlib.pyplot as plt
import os
import pandas as pd
import glob
import config
import myunwrap
import numpy as np
import scipy.constants as C
from sklearn.preprocessing import scale


def phase_heatmap(phase_mat):
    sns.set_context({"figure.figsize": (8, 8)})
    sns.heatmap(phase_mat)
    plt.show()


def normalization(data):
    _range = np.max(data) - np.min(data)
    return (data - np.min(data)) / _range


def phase_scatter(df):
    phases = df["phase"].values
    phases = myunwrap.unwrap(phases)
    times = df["time"].values
    phases = normalization(np.array(phases))
    plt.plot(times, phases)
    plt.show()


if __name__ == '__main__':
    test_path = glob.glob(os.path.join(config.PEN_PATH, '*.csv'))
    file_path = "../20220105104342normal.csv"
    df = pd.read_csv(file_path, header=None)
    df.columns = config.COMMON_COLUMNS
    df["phase"] = 2 * C.pi - df["phase"]
    phase_scatter(df)
    # print(test_path)
    # for path in test_path:
    #     phase_scatter(path)
