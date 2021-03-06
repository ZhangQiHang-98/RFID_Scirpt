# !/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project    ：DataProcess 
@File       ：analysis.py
@Author     ：Zhang Qihang
@Description: 对数据进行分析
@Date       ：2021/12/13 14:48 
"""
import aug_utils
from filters import *
from reproduce_gyro import *
from aug_utils import *
from my_augment import *
from os import listdir
from os.path import isfile, join
from generating import *


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


# 探究功率与频率对相位的关系，计算认证矩阵
def relation_both_with_phase(path):
    df = pd.read_csv(path, header=None)
    df.columns = MORE_COLUMNS
    grouped = df.groupby(['real_freq', 'real_power'])

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
    # 先按照列进行unwrap操作
    for i in range(phase_mat.shape[1]):
        phase_mat[:, i] = myunwrap.unwrap(phase_mat[:, i])
    # 再按照行进行unwrap操作
    for i in range(phase_mat.shape[0]):
        phase_mat[i, :] = myunwrap.unwrap(phase_mat[i, :])
    # 横坐标为功率，纵坐标为相位
    # power_list = get_power_list(20, 30)
    # freq_list = get_freq_list(920.625, 924.125)
    # plot_relation(phase_mat, power_list, freq_list)
    # 返回当前矩阵列表
    return phase_mat


if __name__ == '__main__':
    auth_datas = glob.glob(os.path.join(AUTH_PATH, '*.csv'))

    only_files = [f for f in listdir(PEN_PATH) if isfile(join(PEN_PATH, f))]
    phase_mat = np.zeros(relation_both_with_phase(auth_datas[0]).shape)
    # 计算得到相位矩阵
    for auth_data in auth_datas:
        phase_mat += relation_both_with_phase(auth_data)
    phase_mat /= len(auth_datas)
    print("完成相位矩阵的计算")
    print(phase_mat)
    # 对所有文件进行解码，滤波，插值操作，得到解码后的原始时间序列
    x_train = []
    y_train = []
    classes = set()
    for parent, dirnames, filenames in os.walk(PEN_PATH):
        for filename in filenames:
            filename = os.path.join(parent, filename)
            file_name = filename.split('\\')[-1]
            label = file_name.split('_')[-1]
            label = label[0]
            init_df = pd.read_csv(filename, header=None)
            init_df.columns = MORE_COLUMNS
            # 生成得到了滤波后的解码数据，将其作为原数据输出
            processed_df = gyro(phase_mat, init_df)
            # 进行Unwrap和lowess之后的文件
            processed_df = do_filter(processed_df)
            # 先做一个插值操作，之后就可以舍弃掉time了
            # 这里的y指的是已经根据时间间隔插值后形成的y，x按照linspace生成即可
            y = aug_utils.my_interpolation(processed_df)
            leny = len(y)
            y = y[int(0.05 * leny):int(0.95 * leny)]
            # # 绘制y
            # plt.plot(y)
            # # 将文件名作为图名
            # plt.title(file_name)
            # plt.show()
            # 得到了传统数据增强后的所有序列，将其拼接到一个df中
            trad_aug_ts = tradition_augment(y)
            for ts in trad_aug_ts:
                x_train.append(ts)
                y_train.append(label)
            classes.add(label)
    print("完成传统的数据增强")
    x_train = np.array(x_train)
    y_train = np.array(y_train)
    # 遍历所有文件后，得到了全部文件，进行基于DBA的生成
    syn_num = len(x_train) / len(classes)
    # 由于dtw需要n^2的复杂度，因此应该是先降维再生成，否则时间太长了
    # synthetic_x_train, synthetic_y_train = augment_train_set(x_train, y_train, list(classes), syn_num,
    #                                                         weights_method_name='as')
    print("完成基于DBA的数据增强")
    my_write_file(x_train, y_train, [], [])
