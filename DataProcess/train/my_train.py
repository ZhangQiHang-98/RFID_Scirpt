#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@File    ：my_train.py
@Author  ：Zhang Qihang
@Date    ：2022/4/13 13:28 
@Description : 用自己采集的数据集测试ResNet的效果
"""
import pandas as pd

from utils.constants import UNIVARIATE_ARCHIVE_NAMES as ARCHIVE_NAMES
from utils.constants import MAX_PROTOTYPES_PER_CLASS
from utils.constants import UNIVARIATE_DATASET_NAMES as DATASET_NAMES
import numpy as np
from utils.utils import read_all_datasets
from utils.utils import calculate_metrics
from utils.utils import transform_labels
from utils.utils import create_directory
from resnet import Classifier_RESNET
from my_fcn import Classifier_FCN
from generating import augment_train_set
from sklearn.preprocessing import MinMaxScaler, StandardScaler


def augment_function(augment_algorithm_name, x_train, y_train, classes, N, limit_N=True):
    if augment_algorithm_name == 'as_dtw_dba_augment':
        return augment_train_set(x_train, y_train, classes, N, limit_N=limit_N,
                                 weights_method_name='as', distance_algorithm='dtw'), 'dtw'


def read_data_from_dataset(datasets_dict, use_init_clusters=True):
    x_train = datasets_dict['12345'][0]
    y_train = datasets_dict['12345'][1]
    x_test = datasets_dict['12345'][2]
    y_test = datasets_dict['12345'][3]

    nb_classes = len(np.unique(np.concatenate((y_train, y_test), axis=0)))
    # make the min to zero of labels，让所有标签都是从0到N的
    y_train, y_test = transform_labels(y_train, y_test)

    classes, classes_counts = np.unique(y_train, return_counts=True)
    # 如果是单变量的序列，他的shape=2，这是因为数据集是(N,L)的，也就是有N个长为L的序列
    if len(x_train.shape) == 2:  # if univariate
        # add a dimension to make it multivariate with one dimension
        # 多添加一维，让它变成一维多元的序列
        x_train = x_train.reshape((x_train.shape[0], x_train.shape[1], 1))
        x_test = x_test.reshape((x_test.shape[0], x_test.shape[1], 1))
    # maximum number of prototypes which is the minimum count of a class
    max_prototypes = min(classes_counts.max() + 1,
                         MAX_PROTOTYPES_PER_CLASS + 1)
    init_clusters = None

    return x_train, y_train, x_test, y_test, nb_classes, classes, max_prototypes, init_clusters


# 利用ResNet训练的结果存放的目录
root_deep_learning_dir = '../dataset/my_paper/'

# 数据集存放目录
root_dir_dataset_archive = '../dataset/all_factor_data/'

# 是否采用数据增强
do_augmentation = False
# 采取集成学习
do_ensemble = False

if do_ensemble:
    root_dir_output = root_deep_learning_dir + 'results/ensemble/'
else:
    if do_augmentation:
        root_dir_output = root_deep_learning_dir + 'results/resnet_augment/'
    else:
        root_dir_output = root_deep_learning_dir + 'results/resnet/'


def read_data(file_path):
    data = pd.read_csv(file_path, header=None)
    train_data = data.sample(frac=0.8, random_state=0)
    test_data = data[~data.index.isin(train_data.index)].values
    train_data = train_data.values
    # 取df的20%作为验证集
    x_train = train_data[:, 0:-1]
    y_train = train_data[:, -1]
    x_test = test_data[:, 0:-1]
    y_test = test_data[:, -1]
    # 对训练集和测试集进行标准化处理
    min_max_scaler = MinMaxScaler()
    standard_scaler = StandardScaler()
    # x_train = min_max_scaler.fit_transform(x_train)
    # x_test = min_max_scaler.transform(x_test)
    x_train = standard_scaler.fit_transform(x_train)
    x_test = standard_scaler.transform(x_test)
    datasets_dict = {'12345': (x_train.copy(), y_train.copy(), x_test.copy(), y_test.copy())}
    return datasets_dict


# 先得到x_train，y_train，x_test，y_test
datasets_dict = read_data(root_dir_dataset_archive + 'init_data_330.csv')
x_train, y_train, x_test, y_test, nb_classes, classes, max_prototypes, \
init_clusters = read_data_from_dataset(datasets_dict, use_init_clusters=False)
output_dir = root_dir_output
_, classes_counts = np.unique(y_train, return_counts=True)
nb_prototypes = classes_counts.max()

temp = output_dir
# create the directory if not exists
output_dir = create_directory(output_dir)
# check if directory already exists
if output_dir is None:
    print('the path Already_done' + temp)

if do_ensemble == False:
    classifier = Classifier_RESNET(output_dir, x_train.shape[1:], nb_classes, nb_prototypes, classes,
                                   verbose=True, load_init_weights=do_augmentation)
    y_pred = classifier.fit(x_train, y_train, x_test, y_test)
    df_metrics = calculate_metrics(y_test, y_pred, 0.0)
    df_metrics.to_csv(output_dir + 'df_metrics.csv', index=False)
    print(y_pred)
    print('DONE')
#
# # loop the archive names，给定的只有一个例子
# for archive_name in ARCHIVE_NAMES:
#     # read all the datasets
#     datasets_dict = read_all_datasets(root_dir_dataset_archive, archive_name)
#     # 遍历得到的所有数据集
#     for dataset_name in DATASET_NAMES:
#         print('dataset_name: ', dataset_name)
#         # read dataset
#         x_train, y_train, x_test, y_test, nb_classes, classes, max_prototypes, \
#         init_clusters = read_data_from_dataset(datasets_dict, use_init_clusters=False)
#         # 创建输出目录
#         # 实验结果输出的目录
#         output_dir = root_dir_output + archive_name + '/' + dataset_name + '/'
#
#         _, classes_counts = np.unique(y_train, return_counts=True)
#         # this means that all classes will have a number of time series equal to
#         # nb_prototypes,10
#         nb_prototypes = classes_counts.max()
#
#         temp = output_dir
#         # create the directory if not exists
#         output_dir = create_directory(output_dir)
#         # check if directory already exists
#         if output_dir is None:
#             print('the path Already_done' + temp)
#             continue
#
#         if do_ensemble == False:
#             # 先建立ResNet的网络结构，是否需要初始参数以及是否需要输出
#             classifier = Classifier_RESNET(output_dir, x_train.shape[1:],
#                                            nb_classes, nb_prototypes, classes,
#                                            verbose=True, load_init_weights=do_augmentation)
#             if do_augmentation:
#                 # augment the dataset
#                 syn_train_set, distance_algorithm = augment_function('as_dtw_dba_augment',
#                                                                      x_train, y_train, classes,
#                                                                      nb_prototypes, limit_N=False)
#                 # get the synthetic train and labels
#                 syn_x_train, syn_y_train = syn_train_set
#                 # concat the synthetic with the reduced random train and labels
#                 aug_x_train = np.array(x_train.tolist() + syn_x_train.tolist())
#                 aug_y_train = np.array(y_train.tolist() + syn_y_train.tolist())
#                 # 检测是否引入了新的label
#                 print(np.unique(y_train, return_counts=True))
#                 print(np.unique(aug_y_train, return_counts=True))
#
#                 y_pred = classifier.fit(aug_x_train, aug_y_train, x_test,
#                                         y_test)
#             else:
#                 # no data augmentation
#                 y_pred = classifier.fit(x_train, y_train, x_test,
#                                         y_test)
#
#             df_metrics = calculate_metrics(y_test, y_pred, 0.0)
#             df_metrics.to_csv(output_dir + 'df_metrics.csv', index=False)
#             print('DONE')
#             create_directory(output_dir + 'DONE')
#
#         else:
#             # for ensemble you will have to compute both models in order to ensemble them
#             from ensemble import Classifier_ENSEMBLE
#
#             classifier_ensemble = Classifier_ENSEMBLE(output_dir, x_train.shape[1:], nb_classes, False)
#             classifier_ensemble.fit(x_test, y_test)
