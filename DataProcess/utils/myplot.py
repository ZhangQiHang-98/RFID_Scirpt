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
import numpy as np


def phase_heatmap(phase_mat):
    # sns.set_context({"figure.figsize": (8, 8)})
    # sns.heatmap(phase_mat)
    temp = []
    for i in range(phase_mat.shape[0]):
        temp.append(phase_mat[i][3])
    print(temp)
    plt.scatter(np.arange(len(temp)), temp)
    plt.show()


def phase_scatter(phase_list):
    plt.scatter(np.arange(len(phase_list)), phase_list)
    plt.show()
