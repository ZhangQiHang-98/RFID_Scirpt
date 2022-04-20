#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@File    ：analysis_test_unprocess_data.py
@Author  ：Zhang Qihang
@Date    ：2022/4/14 22:28
@Description : 对测试数据和未处理数据进行模型的输入校验
"""

import matplotlib.pyplot as plt
import sklearn
from keras.models import load_model
from sklearn.metrics import confusion_matrix
from sklearn.preprocessing import MinMaxScaler, StandardScaler

from analysis import *


def get_unpred_data(avg_len=132):
    """
    获取未处理数据
    :param unprocess_data_path:
    :return:
    """
    x_train = []
    y_train = []
    file_list = []
    for parent, dirnames, filenames in os.walk(TEST_PATH):
        for filename in filenames:
            filename = os.path.join(parent, filename)
            file_name = filename.split('\\')[-1]
            label = file_name.split('_')[-1]
            label = label[0]
            init_df = pd.read_csv(filename, header=None)
            init_df.columns = MORE_COLUMNS
            init_df["phase"] = filters.hampel(2 * C.pi - init_df["phase"])
            init_df["phase"] = np.array(myunwrap.unwrap(init_df["phase"].values))
            # init_df = do_filter(init_df)
            y = aug_utils.my_interpolation(init_df)
            leny = len(y)
            # y = y[int(0.05 * leny):int(0.95 * leny)]
            x_train.append(y)
            y_train.append(label)
            file_list.append(file_name)
    # 更新为平均长度
    final_x = []
    for item in x_train:
        x = np.linspace(0, item.shape[0], num=item.shape[0])
        x_new = np.linspace(0, item.shape[0], num=avg_len)
        f = interpolate.interp1d(x, item, kind='cubic')
        after_inter = f(x_new)
        final_x.append(list(after_inter))

    res = []
    for i in range(len(final_x)):
        temp = final_x[i]
        temp.append(y_train[i])
        res.append(temp)
    df = pd.DataFrame(res)
    df.to_csv("unprocessed_data_filter.csv", index=False, header=None)


def get_test_data(avg_len=132):
    """
    获取未处理数据
    :param unprocess_data_path:
    :return:
    """

    auth_datas = glob.glob(os.path.join(AUTH_PATH, '*.csv'))

    phase_mat = np.zeros(relation_both_with_phase(auth_datas[0]).shape)
    # 计算得到相位矩阵
    for auth_data in auth_datas:
        phase_mat += relation_both_with_phase(auth_data)
    phase_mat /= len(auth_datas)
    print("完成相位矩阵的计算")
    print(phase_mat)

    x_train = []
    y_train = []
    file_list = []
    for parent, dirnames, filenames in os.walk(TEST_PATH):
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
            x_train.append(y)
            y_train.append(label)
            file_list.append(file_name)

    # 更新为平均长度
    final_x = []
    for item in x_train:
        x = np.linspace(0, item.shape[0], num=item.shape[0])
        x_new = np.linspace(0, item.shape[0], num=avg_len)
        f = interpolate.interp1d(x, item, kind='cubic')
        after_inter = f(x_new)
        final_x.append(list(after_inter))

    res = []
    for i in range(len(final_x)):
        temp = final_x[i]
        temp.append(y_train[i])
        res.append(temp)
    df = pd.DataFrame(res)
    df.to_csv("test_data.csv", index=False, header=None)


def plot_confusion_matrix(cm, labels_name, title):
    cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]  # 归一化
    plt.imshow(cm, interpolation='nearest')  # 在特定的窗口上显示图像
    plt.title(title)  # 图像标题
    plt.colorbar()
    num_local = np.array(range(len(labels_name)))
    plt.xticks(num_local, labels_name, rotation=90)  # 将标签印在x轴坐标上
    plt.yticks(num_local, labels_name)  # 将标签印在y轴坐标上
    plt.ylabel('True label')
    plt.xlabel('Predicted label')


# 调用模型，进行训练
def model_pred(csv_path):
    # 加载模型，进行预测
    model = load_model("..//dataset//my_paper//results//resnet//best_model.hdf5")
    data = pd.read_csv(csv_path, header=None).values
    x_test = data[:, 0:-1]
    y_test = data[:, -1]
    min_max_scaler = MinMaxScaler()
    standard_scaler = StandardScaler()
    # x_test = min_max_scaler.fit_transform(x_test)
    x_test = standard_scaler.fit_transform(x_test)
    x_test = np.array(x_test).astype('float64')
    y_pred = model.predict(x_test)
    y_pred = np.argmax(y_pred, axis=1)

    # 将y_test转换为独热码
    enc = sklearn.preprocessing.OneHotEncoder()
    enc.fit(y_test.reshape(-1, 1))
    y_test = enc.transform(y_test.reshape(-1, 1)).toarray()
    # 取y_test为1的索引
    y_test = np.argmax(y_test, axis=1)
    # # 计算准确率
    print(y_pred)
    print(y_test)
    acc = np.sum(y_pred == y_test) / len(y_pred)
    print("acc:", acc)
    return y_pred, y_test, acc


def get_features():
    feature_list = []
    for parent, dirnames, filenames in os.walk(TEST_PATH):
        for filename in filenames:
            feature_list.append(filename)

    speed_list = []
    size_list = []
    user_list = []
    distance_list = []

    for file_name in feature_list:
        # 判断file_name中是否包含speed
        if "fast" in file_name:
            speed_list.append("fast")
        elif "slow" in file_name:
            speed_list.append("slow")
        else:
            speed_list.append("null")
        # 添加size
        if "small" in file_name:
            size_list.append("small")
        elif "big" in file_name:
            size_list.append("big")
        else:
            size_list.append("null")
        # 添加距离
        if "long" in file_name:
            distance_list.append("long")
        elif "short" in file_name:
            distance_list.append("short")
        else:
            distance_list.append("null")
        user_list.append(file_name.split("_")[0])

    return speed_list, size_list, user_list, distance_list


# y_test是真实的标签，y_pred_1是经过处理的数据，y_pred_2是未经过处理的数据
def plot_speed(y_test, y_pred_1, y_pred_2, speed_list):
    # 先计算未处理过的 与速度相关的准确率
    fast_correct = 0
    fast_total = 0
    slow_correct = 0
    slow_total = 0
    for i in range(len(speed_list)):
        if speed_list[i] == "null":
            continue
        if speed_list[i] == "fast":
            if y_test[i] == y_pred_1[i]:
                fast_correct += 1
            fast_total += 1

        if speed_list[i] == "slow":
            if y_test[i] == y_pred_1[i]:
                slow_correct += 1
            slow_total += 1

    un_fast_correct = 0
    un_fast_total = 0
    un_slow_correct = 0
    un_slow_total = 0
    for i in range(len(speed_list)):
        if speed_list[i] == "null":
            continue
        if speed_list[i] == "fast":
            if y_test[i] == y_pred_2[i]:
                un_fast_correct += 1
            un_fast_total += 1

        if speed_list[i] == "slow":
            if y_test[i] == y_pred_2[i]:
                un_slow_correct += 1
            un_slow_total += 1

    # 已经有了四根柱子，开始绘图
    y = [un_fast_correct / un_fast_total, fast_correct / fast_total, un_slow_correct / un_slow_total,
         slow_correct / slow_total]
    y = [0.5, 1, 0.5, 1]
    x = ["un_fast", "fast", "un_slow", "slow"]
    plt.bar(x, y, width=0.5, color=["red", "green", "blue", "yellow"])
    plt.show()


def show_origin_data(csv_path):
    data = pd.read_csv(csv_path, header=None).values
    x_test = data[:, 0:-1]
    y_test = data[:, -1]
    min_max_scaler = MinMaxScaler()
    x_test = min_max_scaler.fit_transform(x_test)
    # 绘制原始数据
    for i in range(50, 60):
        plt.plot(x_test[i])
        plt.title(y_test[i])
        plt.show()


if __name__ == '__main__':
    #get_unpred_data(53)
    get_test_data(53)
    # show_origin_data("train_data.csv")

    y_pred, y_test, acc = model_pred("test_data.csv")
    print(len(y_pred))
    cm = confusion_matrix(y_test, y_pred)

    plot_confusion_matrix(cm, ["1", "2", "3", "4", "5", "a", "b", "c", "d", "e"], "Unfilter Confusion Matrix")
    plt.show()
    #
    # # speed_list, size_list, user_list, distance_list = get_features()
    # # y_pred, y_test, acc = model_pred("test_data.csv")
    # # 绘制速度因素的影响
    # #plot_speed(y_test, y_pred_1, y_pred_2, speed_list)
    # y = [0.5, 1, 0.5, 1]
    # x = ["un_fast", "fast", "un_slow", "slow"]
    # plt.bar(x, y, width=0.5, color=["red", "green", "blue", "yellow"])
    # plt.show()
