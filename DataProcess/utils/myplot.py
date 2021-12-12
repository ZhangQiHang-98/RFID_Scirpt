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


def phase_heatmap(phase_mat):
    sns.set_context({"figure.figsize": (8, 8)})
    sns.heatmap(phase_mat)
    plt.show()


def phase_scatter(path):

    df = pd.read_csv(path)
    df.columns = config.COMMON_COLUMNS
    phases = df["phase"].values
    phases = myunwrap.unwrap(phases)
    times = df["time"].values
    plt.scatter(times, phases)
    plt.show()


if __name__ == '__main__':
    test_path = glob.glob(os.path.join(config.PEN_PATH, '*.csv'))
    print(test_path)
    for path in test_path:
        phase_scatter(path)
