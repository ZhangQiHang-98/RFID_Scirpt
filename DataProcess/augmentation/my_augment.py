# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：my_augment.py
@Author     ：Zhang Qihang
@Description: 进行时序数据的数据增强
@Date       ：2022/1/10 15:19 
"""


def tradition_augment(data_paths):
    """
    传统的时间序列增强方法也就是几何变换，共有以下几种方法：
    1. WS(Window Slicing)，设置窗口大小(90%)，从时间序列中提取窗口大小的切片，可以拉长也可以不拉长至原始长度。
    2. WW(Window Wraping)，对部分片段(10%)进行拉伸或压缩，然后将时间序列统一到原始序列的长度
    3. 添加噪声，增强数据的防过拟合能力。
    4. Scaling，将整个数组进行缩放
    额外的一些方法：
    * Perm(Permutation，排列)，将数据分割为N(1~5)个样本段，然后随机排列这些样本段（感觉会打乱时序性 ）
    Args:
        data_paths: 某一类动作的时间序列
    Returns:

    """
    pass


def all_augment(data_paths):
    """
    对一类动作的时间序列进行数据增强，暂定先进行传统的时序增强，然后进行加权DBA增强。
    Args:
        data_paths: 某一类动作的时间序列
    Returns:

    """


if __name__ == '__main__':
    # 进行窗口移动()
    pass
