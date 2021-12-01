# ###最小二乘法试验###
# import numpy as np
# from scipy.optimize import leastsq
# import matplotlib.pyplot as plt
#
# # 采样点(Xi,Yi)
# Xi = np.array([1, 2, 3, 4, 5])
# Yi = np.array([1, 2, 3, 4, 5])
#
#
# ###需要拟合的函数func及误差error###
# def func(p, x):
#     k, b = p
#     return k * x + b
#
#
# def error(p, x, y, s):
#     print(s)
#     return func(p, x) - y  # x、y都是列表，故返回值也是个列表
#
#
# # TEST
# # k和b的初始值，会随着迭代慢慢减小 可以任意设定,经过几次试验，发现p0的值会影响cost的值
# p0 = [100, 2]
# # print( error(p0,Xi,Yi) )
#
# ###主函数从此开始###
# s = "Test the number of iteration"  # 试验最小二乘法函数leastsq得调用几次error函数才能找到使得均方误差之和最小的k、b
# Para = leastsq(error, p0, args=(Xi, Yi, s))  # 把error函数中除了p以外的参数打包到args中
# k, b = Para[0]
# print(k, b)
#
# ###绘图，看拟合效果###
# plt.figure(figsize=(8, 6))
# plt.scatter(Xi, Yi, color="red", label="Sample Point", linewidth=3)  # 画样本点
# x = np.linspace(0, 10, 1000)
# y = k * x + b
# plt.plot(x, y, color="orange", label="Fitting Line", linewidth=2)  # 画拟合直线
# plt.legend()
# plt.show()
a = []
print(le)