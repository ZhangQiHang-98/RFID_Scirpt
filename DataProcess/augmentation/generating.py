# this contains the data generation methods of icdm 2017
# "Generating synthetic time series to augment sparse datasets"
import numpy as np
import random
import utils

from dba import calculate_dist_matrix
from dba import dba
from knn import get_neighbors


# weights calculation method : Average Selected (AS)，选择论文中提到的第二种模式
def get_weights_average_selected(x_train, dist_pair_mat, distance_algorithm='dtw'):
    # get the distance function
    dist_fun = utils.constants.DISTANCE_ALGORITHMS[distance_algorithm]
    # get the distance function params
    dist_fun_params = utils.constants.DISTANCE_ALGORITHMS_PARAMS[distance_algorithm]
    # get the number of dimenions
    num_dim = x_train[0].shape[1]
    # number of time series
    n = len(x_train)
    # maximum number of K for KNN
    max_k = 5
    # maximum number of sub neighbors
    max_subk = 2
    # get the real k for knn
    k = min(max_k, n - 1)
    # make sure
    subk = min(max_subk, k)
    # the weight for the center，随机选择一个中心序列，将其权重设置为0.5
    weight_center = 0.5
    # the total weight of the neighbors，他最近的两个邻居共享0.3的权重
    weight_neighbors = 0.3
    # total weight of the non neighbors，其余邻居共享0.2的权重
    weight_remaining = 1.0 - weight_center - weight_neighbors
    # number of non neighbors
    n_others = n - 1 - subk
    # get the weight for each non neighbor，fill_value存储非邻居的统一权重
    if n_others == 0:
        fill_value = 0.0
    else:
        fill_value = weight_remaining / n_others
    # choose a random time series
    idx_center = random.randint(0, n - 1)
    # get the init dba
    init_dba = x_train[idx_center]
    # init the weight matrix or vector for univariate time series
    weights = np.full((n, num_dim), fill_value, dtype=np.float64)
    # fill the weight of the center
    weights[idx_center] = weight_center
    # find the top k nearest neighbors，按照论文中的是找到最近的5个序列的索引
    topk_idx = np.array(get_neighbors(x_train, init_dba, k, dist_fun, dist_fun_params,
                                      pre_computed_matrix=dist_pair_mat,
                                      index_test_instance=idx_center))
    # select a subset of the k nearest neighbors，最终随机选择了其中的两个
    final_neighbors_idx = np.random.permutation(k)[:subk]
    # adjust the weight of the selected neighbors
    weights[topk_idx[final_neighbors_idx]] = weight_neighbors / subk
    # return the weights and the instance with maximum weight (to be used as
    # init for DBA )
    return weights, init_dba


# 数据增强的入口
def augment_train_set(x_train, y_train, classes, N, dba_iters=5,
                      weights_method_name='aa', distance_algorithm='dtw',
                      limit_N=True):
    """
    This method takes a dataset and augments it using the method in icdm2017.
    :param x_train: The original train set
    :param y_train: The original labels set
    :param classes: 序列标签名数组[a,b,c,d,e]
    :param N: The number of synthetic time series. 想要生成多少个新的时间序列
    :param dba_iters: The number of dba iterations to converge.
    :param weights_method_name: The method for assigning weights (see constants.py) 默认是AS
    :param distance_algorithm: The name of the distance algorithm used (see constants.py)
    :param limit_N:
    """
    # get the weights function
    weights_fun = utils.constants.WEIGHTS_METHODS[weights_method_name]
    # get the distance function
    dist_fun = utils.constants.DISTANCE_ALGORITHMS[distance_algorithm]
    # get the distance function params
    dist_fun_params = utils.constants.DISTANCE_ALGORITHMS_PARAMS[distance_algorithm]
    # synthetic train set and labels
    synthetic_x_train = []
    synthetic_y_train = []
    # 循环每个类，对该类下的x_train进行生成
    for c in classes:
        # get the MTS for this class
        c_x_train = x_train[np.where(y_train == c)]

        if len(c_x_train) == 1:
            # 如果这个类只有一个序列
            # skip if there is only one time series per set
            continue

        if limit_N:
            # limit the nb_prototypes
            nb_prototypes_per_class = min(N, len(c_x_train))
        else:
            # number of added prototypes will re-balance classes
            nb_prototypes_per_class = N + (N - len(c_x_train))

        # get the pairwise matrix
        if weights_method_name == 'aa':
            # then no need for dist_matrix
            dist_pair_mat = None
        else:
            # 不是aa模式的话，需要DTW矩阵
            dist_pair_mat = calculate_dist_matrix(c_x_train, dist_fun, dist_fun_params)
        # loop through the number of synthtectic examples needed
        for n in range(nb_prototypes_per_class):
            # get the weights and the init for avg method
            weights, init_avg = weights_fun(c_x_train, dist_pair_mat,
                                            distance_algorithm=distance_algorithm)
            # get the synthetic data
            synthetic_mts = dba(c_x_train, dba_iters, verbose=False,
                                distance_algorithm=distance_algorithm,
                                weights=weights,
                                init_avg_method='manual',
                                init_avg_series=init_avg)
            # add the synthetic data to the synthetic train set
            synthetic_x_train.append(synthetic_mts)
            # add the corresponding label
            synthetic_y_train.append(c)
    # return the synthetic set
    return np.array(synthetic_x_train), np.array(synthetic_y_train)
