# UNWRAP（P）通过将大于pi的绝对跳跃改变为2 * pi补码来展开弧度相位P.
import math
import copy


def unwrap(raw_vector, cutoff=math.pi):
    if raw_vector is None or len(raw_vector) <= 0:
        print("warring: The vector is empty!")
        return None
    vector = list(copy.deepcopy(raw_vector))
    # 初始化变量
    m = len(vector)
    dp = []
    dp_corr = []
    roundDown = []
    cumsum = []
    # 计算递增的相位值
    for i in range(m - 1):
        dp.append(vector[i + 1] - vector[i])
    # 计算递增的相位值偏离多少个2PI
    for i in range(len(dp)):
        dp_corr.append(dp[i] / (2 * math.pi))
    # 对dp_corr进行舍入，以达到（2n+1）pi 被规整到2n*pi,而不是（2n+2）pi
    for i in range(len(dp_corr)):
        roundDown.append(abs(dp_corr[i] % 1) <= 0.5)
    # 按照以上注释中的思路整理数据
    for i in range(len(dp_corr)):
        if roundDown[i] == True:
            if dp_corr[i] > 0:
                dp_corr[i] = math.floor(dp_corr[i])
            else:
                dp_corr[i] = math.ceil(dp_corr[i])
        # 朝最近整数四舍五入
        else:
            dp_corr[i] = round(dp_corr[i])

    # 处理跳变，跳变大于cutoff才处理
    # cumsum 用来记录前i个跳变的累计影响，每前一个元素的加减，后续元素都要加减
    for i in range(len(dp)):
        if abs(dp[i]) < cutoff:
            dp_corr[i] = 0
        if i == 0:
            cumsum.append(dp_corr[i])
        else:
            cumsum.append(dp_corr[i] + cumsum[i - 1])

    # 规整
    for i in range(1, m):
        vector[i] = vector[i] - cumsum[i - 1] * 2 * math.pi

    # 对于数据点稀疏的情况，有些点的跳变没有超过2pi，需要进一步微调
    jumpoint = 3
    for i in range(m - 1):
        if vector[i + 1] - vector[i] > jumpoint:
            for j in range(i + 1, m):
                vector[j] = vector[j] - 2 * math.pi
            continue
        if vector[i] - vector[i + 1] > jumpoint:
            for j in range(i + 1, m):
                vector[j] = vector[j] + 2 * math.pi
    return vector
