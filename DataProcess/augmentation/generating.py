# this contains the data generation methods of icdm 2017
# "Generating synthetic time series to augment sparse datasets"
import numpy as np
import utils.constants

from dba import calculate_dist_matrix
from dba import dba


def my_generating(x_train):
    pass


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

    # reshape所有的x_train，报warning是因为长度不等。
    x_train = np.array([item.reshape(item.shape[0], 1) for item in x_train])
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
        print(dist_pair_mat)
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
