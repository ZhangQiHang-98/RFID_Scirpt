import numpy as np
import utils
from knn import get_neighbors
import random


# 计算给定序列集的两两之间的DTW距离
def calculate_dist_matrix(tseries, dist_fun, dist_fun_params):
    # 将tseries中的时间序列进行reshape
    N = len(tseries)
    pairwise_dist_matrix = np.zeros((N, N), dtype=np.float64)
    # pre-compute the pairwise distance  计算两两序列的DTW距离
    for i in range(N - 1):
        x = tseries[i]
        for j in range(i + 1, N):
            y = tseries[j]
            # dist为x,y两个时间序列的DTW距离的平方
            dist = dist_fun(x, y, **dist_fun_params)[0]
            # because dtw returns the sqrt
            dist = dist * dist
            pairwise_dist_matrix[i, j] = dist
            # dtw is symmetric 对称的填入另一半即可
            pairwise_dist_matrix[j, i] = dist
        # 自己到自己的序列为0
        pairwise_dist_matrix[i, i] = 0
    return pairwise_dist_matrix


def medoid(tseries, dist_fun, dist_fun_params):
    """
    Calculates the medoid of the given list of MTS
    :param tseries: The list of time series 
    """
    N = len(tseries)
    if N == 1:
        return 0, tseries[0]
    # 记录的是各个序列之间的DTW距离
    pairwise_dist_matrix = calculate_dist_matrix(tseries, dist_fun,
                                                 dist_fun_params)
    # 选一条序列，该序列到其他的序列的总DTW距离最小
    sum_dist = np.sum(pairwise_dist_matrix, axis=0)
    min_idx = np.argmin(sum_dist)
    med = tseries[min_idx]
    return min_idx, med


# 对应算法中的迭代求解DBA阶段，一次更新的过程
def _dba_iteration(tseries, avg, dist_fun, dist_fun_params, weights):
    """
    Perform one weighted dba iteration and return the new average 
    """
    # the number of time series in the set
    n = len(tseries)
    # length of the time series，DBA序列的长度
    ntime = avg.shape[0]
    # number of dimensions (useful for MTS)，DBA序列的维度
    num_dim = avg.shape[1]
    # array containing the new weighted average sequence 
    new_avg = np.zeros((ntime, num_dim), dtype=np.float64)
    # array of sum of weights 
    sum_weights = np.zeros((ntime, num_dim), dtype=np.float64)
    # loop the time series 
    for s in range(n):
        # 循环取时间序列列表中的每个序列
        series = tseries[s]
        # 计算他和初始序列的DTW距离矩阵
        dtw_dist, dtw = dist_fun(avg, series, **dist_fun_params)
        # 从大到小进行推导
        i = ntime
        j = series.shape[0]
        # i是当前DBA序列的长度，j是当前选出的序列的长度
        # 此处的while循环是记录s与avg序列的DTW序列
        while i >= 1 and j >= 1:
            # 如果分配了权重的话，就按照权重分配，这是由于我们认为某些序列的贡献更大
            # 权重并不影响DTW的计算公式，不会改变序列集和T之间的映射关系
            new_avg[i - 1] += series[j - 1] * weights[s]
            sum_weights[i - 1] += weights[s]

            a = dtw[i - 1, j - 1]
            b = dtw[i, j - 1]
            c = dtw[i - 1, j]
            if a < b:
                if a < c:
                    # a is the minimum
                    i -= 1
                    j -= 1
                else:
                    # c is the minimum
                    i -= 1
            else:
                if b < c:
                    # b is the minimum
                    j -= 1
                else:
                    # c is the minimum
                    i -= 1
    # update the new weighted avgerage 
    new_avg = new_avg / sum_weights

    return new_avg


def dba(tseries, max_iter=10, verbose=False, init_avg_method='medoid',
        init_avg_series=None, distance_algorithm='dtw', weights=None):
    """
    Computes the Dynamic Time Warping (DTW) Barycenter Averaging (DBA) of a 
    group of Multivariate Time Series (MTS). 
    :param tseries: A list containing the series to be averaged, where each 
        MTS has a shape (l,m) where l is the length of the time series and 
        m is the number of dimensions of the MTS - in the case of univariate 
        time series m should be equal to one
        一组时间序列，样式均为(l,m)
    :param max_iter: The maximum number of iterations for the DBA algorithm.
    :param verbose: If true, then provide helpful output.
    :param init_avg_method: Either: 
        'random' the average will be initialized by a random time series, 
        'medoid'(default) the average will be initialized by the medoid of tseries, 
        'manual' the value in init_avg_series will be used to initialize the average # 选择指定的序列当初始序列
    :param init_avg_series: this will be taken as average initialization if 
        init_avg_method is set to 'manual'
    :param distance_algorithm: Determine which distance to use when aligning 
        the time series
    :param weights: An array containing the weights to calculate a weighted dba
        (NB: for MTS each dimension should have its own set of weights)
        expected shape is (n,m) where n is the number of time series in tseries 
        and m is the number of dimensions
    """
    # get the distance function 
    dist_fun = utils.constants.DISTANCE_ALGORITHMS[distance_algorithm]
    # get the distance function params  默认dtw不使用扭曲窗口
    dist_fun_params = utils.constants.DISTANCE_ALGORITHMS_PARAMS[distance_algorithm]
    # check if given dataset is empty 
    if len(tseries) == 0:
        # then return a random time series because the average cannot be computed 
        start_idx = np.random.randint(0, len(tseries))
        return np.copy(tseries[start_idx])

    # init DBA
    if init_avg_method == 'medoid':
        avg = np.copy(medoid(tseries, dist_fun, dist_fun_params)[1])
    elif init_avg_method == 'random':
        start_idx = np.random.randint(0, len(tseries))
        avg = np.copy(tseries[start_idx])
    else:  # init with the given init_avg_series
        avg = np.copy(init_avg_series)

    if len(tseries) == 1:
        return avg
    if verbose == True:
        print('Doing iteration')

    # main DBA loop 对应算法中的DBA更新
    for i in range(max_iter):
        if verbose == True:
            print(' ', i, '...')
        if weights is None:
            # 如果所有时间序列的权重均为1，那么相当于是非加权版本的DBA
            # when giving all time series a weight equal to one we have the 
            # non - weighted version of DBA 
            weights = np.ones((len(tseries), tseries[0].shape[1]), dtype=np.float64)
        # dba iteration 
        avg = _dba_iteration(tseries, avg, dist_fun, dist_fun_params, weights)

    return avg


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
