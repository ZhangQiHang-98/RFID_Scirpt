package com.rfid.config;

import lombok.Data;

/**
 * @className: Myconfig
 * @Description: 自建的配置类
 * @author: Zhang Qihang
 * @date: 2021/11/23 15:02
 */
public class Myconfig {

    // 阅读器地址
    public static String hostname = "192.168.0.221";

    public static Integer minCounts = 5;

    // 默认天线功率
    public static double TxPowerinDbm = 25;

    // 接收灵敏度设定
    public static double RxSensitivityinDbm = -70.0;
    // 阅读器模式
    public static String mode = "MaxThroughput";
    // 最小停留时间
    public static long minStayTime = 200;
    // 最大停留时间
    public static long maxStayTime = 1000;
    // 指纹库收集时的停留时间
    public static long collectTime = 1000;
    // 默认频率列表
    public static double[] freqList = getFreqList(920.625, 924.125);
    // 采集数据存放位置
    public static String filePath = "D:\\RFID_Scirpt\\data\\my_paper\\";

    // 生成频率范围内的所有频率列表
    public static double[] getFreqList(Double startFreq, Double endFreq) {

        // 根据最小间隔0.25Mhz从920.625 MHz to 924.375生成频率列表
        double[] freqList = new double[(int) ((endFreq - startFreq) / 0.25 + 1)];
        for (int i = 0; i < freqList.length; i++) {
            freqList[i] = startFreq + i * 0.25;
        }
        return freqList;
    }

    // 随机生成当前停留时间
    public static long getStayTime() {
        return (long) (Math.random() * (maxStayTime - minStayTime) + minStayTime);
    }
}
