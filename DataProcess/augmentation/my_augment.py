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
    # 先切片(取整个序列的大部分)
    temp = window_slicing(time_series)
    for item in temp:
        aug.append(item)
    # 然后窗口规整（对窗口的一小部分进行拉伸或者缩减）
    temp = window_wraping(time_series)
    for item in temp:
        aug.append(item)
    # 进行传统增强，目前只选择添加噪声，mag_warp和time_warp三种
    for s in aug:
        s = add_noise(s)
        s = mag_warp(s)
        s = time_warp(s)
    return aug


def all_augment(time_series):
    """
    对一类动作的时间序列进行数据增强，暂定先进行传统的时序增强，然后进行加权DBA增强。
    Args:
        data_paths: 传入的是某一类动作的时间序列，一个list[array[int]],注意的是长度不相等
    Returns:
    """
    after_trad_aug = tradition_augment(time_series)
    # 最终在传统增强完之后，应该就生成csv文件才对，然后基于DBA的方法再去根据每一类不同的csv文件进行累加生成


if __name__ == '__main__':
    # 进行窗口移动()
    pass
