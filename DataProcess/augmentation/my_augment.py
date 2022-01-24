# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：my_augment.py
@Author     ：Zhang Qihang
@Description: 进行时序数据的数据增强
@Date       ：2022/1/10 15:19 
"""
from tradition_augmentation import *


def tradition_augment(time_series):
    """
    传统的时间序列增强方法也就是几何变换，详细介绍参照tradition_augmentation.py
    Args:
        time_series: 某一类动作的时间序列
    Returns:

    """
    aug = []

    for series in time_series:
        # 先切片
        temp = window_slicing(series)
        for item in temp:
            aug.append(item)
        # 然后窗口规整
        temp = window_wraping(series)
        for item in temp:
            aug.append(item)
        aug.append(series)

    # 进行传统增强
    for s in aug:
        s = add_noise(s)
        s = mag_warp(s)
        s = time_warp(s)
    return s


def all_augment(time_series):
    """
    对一类动作的时间序列进行数据增强，暂定先进行传统的时序增强，然后进行加权DBA增强。
    Args:
        data_paths: 传入的是某一类动作的时间序列，一个list[array[int]],注意的是长度不相等
    Returns:
    """
    after_trad_aug = tradition_augment(time_series)



if __name__ == '__main__':
    # 进行窗口移动()
    pass
