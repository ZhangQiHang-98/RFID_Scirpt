package com.rfid.config;

import java.util.Arrays;

/**
 * @Description 用于验证ReaderPrint的阅读器配置
 * @Author Zhang Qihang
 * @Date 2021/10/27 13:47
 */
public class ReaderPrintConfig {
    // 阅读器地址：192.168.0.221
    public static String hostname = "192.168.0.221";
    // 天线端口选择  R220:{1,2}  R420:{1,2,3,4}
    public static short[] port = new short[]{1};
    // 传输功率设定
    public static double TxPowerinDbm = 25;
    // 接收灵敏度设定
    public double RxSensitivityinDbm = -70.0;
    // 阅读器模式
    public static String mode = "MaxThroughput";
    // 标签过滤设定
    public static String targetMask = "";
    // 频率列表
    //public static double[] freqList = new double[]{920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375};
    public static double[] freqList = getFreqList(920.625, 921.125);

    // 功率列表
    //public static double[] powerList = new double[]{20,22,24,26,28,30};
    public static double[] powerList = getPowerList(20.0, 21.0);
    // 读取时间
    public static long duration = 2000;

    // 采集的数据存放的位置
    public static String filePath = "D:\\RFID_Scirpt\\data\\read_print\\";

    // 生成频率范围内的所有频率列表
    public static double[] getFreqList(Double startFreq, Double endFreq) {

        // 根据最小间隔0.25Mhz从920.625 MHz to 924.375生成频率列表
        double[] freqList = new double[(int) ((endFreq - startFreq) / 0.25 + 1)];
        for (int i = 0; i < freqList.length; i++) {
            freqList[i] = startFreq + i * 0.25;
        }
        return freqList;
    }

    // 生成功率范围内的所有功率列表
    public static double[] getPowerList(Double startPower, Double endPower) {
        // 根据最小间隔0.25db从10db到31db生成功率列表
        double[] powerList = new double[(int) ((endPower - startPower) / 0.25 + 1)];
        for (int i = 0; i < powerList.length; i++) {
            powerList[i] = startPower + i * 0.25;
        }
        return powerList;
    }


}

